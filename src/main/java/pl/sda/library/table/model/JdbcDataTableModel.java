package pl.sda.library.table.model;

import java.math.BigDecimal;
import java.sql.*;
import java.util.LinkedList;
import java.util.List;

import pl.sda.library.model.Book;

public class JdbcDataTableModel extends CrudDataTableModel {

	private static final long serialVersionUID = 1L;

	//dodane
	private static final String DB_DRIVER = "com.mysql.jdbc.Driver";
	private static final String DB_URL = "jdbc:mysql://xxx/xxx?useSSL=false";
	private static final String DB_USER = "xxx";
	private static final String DB_PASSWORD = "xxx";
	//dodane

	public JdbcDataTableModel() {

		//dodane
		try {
			Class.forName(DB_DRIVER);
		}
		catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		//dodane

		filterByName("");
	}

	@Override
	public int getRowCount() {
		//TODO liczba książek
		BigDecimal numberOfBooks = null;
		Connection connection = null;
		try {
			//connection string
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

			//tworzymy  boekt statement, ktory pozwala wyslac zapytanie
			Statement statement = connection.createStatement();

			//wykonujemy zapytanie, wyniki laduja w resultSet
			ResultSet resultSet = statement.executeQuery("select count(*) from book");

			if (resultSet.next()) { //czy w result set sa jakies wartosci?
				//count(*) ktory tu przyjdzie bedzie zmapowany na BigDecimal !!!
				numberOfBooks = resultSet.getBigDecimal(1);//1 - czy !!!li pierwsza kolumna z zapytania
			}


		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
            try {
				if (connection != null) { //jesli driver manager nie zadziala (np.zle haslo)
					connection.close();		//to tutaj connection bedzie nullem
				}
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return numberOfBooks.intValue();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Book book = getByName(filter).get(rowIndex);
		switch (columnIndex) {
			case 0:
				return book.getId();
			case 1:
				return book.getTitle();
			case 2:
				return book.getAuthorFirstName();
			case 3:
				return book.getAuthorLastName();
			case 4:
				return book.getCategories();
			default:
				return null;
		}
	}

	@Override
	public Book getById(int id) {
		//TODO książka na podstawie id
        Book bookToReturn = new Book();
        Connection connection = null;

        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            PreparedStatement statement = connection.prepareStatement("select b.title as title,b.id as id,"
                    + "a.first_name as firstName,a.last_name as lastName "
                    + "from book as b join author as a on b.author_id = a.id where b.id = ?");
            statement.setInt(1, id);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                bookToReturn.setId(resultSet.getInt("id"));
                bookToReturn.setTitle(resultSet.getString("title"));
                bookToReturn.setAuthorFirstName(resultSet.getString("firstName"));
                bookToReturn.setAuthorLastName(resultSet.getString("lastName"));
            }
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            try {
				if (connection != null) { //jesli driver manager nie zadziala (np.zle haslo)
					connection.close();		//to tutaj connection bedzie nullem
				}
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return bookToReturn;
	}

	@Override
	public List<Book> getByName(String name) {
		//TODO książki na podstawie nazwy
		List<Book> bookList = new LinkedList<>();//lista naszych ksiazek
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			Statement statement = connection.createStatement();
			ResultSet resultSet = statement.executeQuery("select b.id id, b.title title, " +
                    "a.first_name firstName, a.last_name lastName " +
                                                                        "from book b" +
                    " left join author a on (b.author_id = a.id)");

			while(resultSet.next()) {
				Book book = new Book(); //nowa ksiazka
				String title = resultSet.getString("title"); //pobieram wartosc zzapytania
				book.setTitle(title); //uaktulaniam info o nowejksizce
                int id = resultSet.getInt("id");
                book.setId(id);
                String firstName = resultSet.getString("firstName");
                book.setAuthorFirstName(firstName);
                String lastName = resultSet.getString("lastName");
                book.setAuthorLastName(lastName);
                bookList.add(book); //i dodaje ja do listy
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		finally {
			try {
				if (connection != null) { //jesli driver manager nie zadziala (np.zle haslo)
					connection.close();		//to tutaj connection bedzie nullem
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return bookList;
	}

	@Override
	public void create(Book book) {
		//TODO dodanie książki
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            connection.setAutoCommit(false);

            PreparedStatement statement = connection.prepareStatement("insert into author(first_name, last_name) " +
                    "values (?, ?)", Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, book.getAuthorFirstName());
            statement.setString(2, book.getAuthorLastName());
            statement.executeUpdate(); //dla insertrow i updateow robimu wlasnie te funkcje
            //ona nie zwraca zadnego resultSet-u !!!

            int authorId = 0;
            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                authorId = generatedKeys.getInt(1); //zwrocone id bedzie w 1 kolumnie zwrocnego info
            }

            statement.close();

            PreparedStatement statement2 = connection.prepareStatement("insert into book(title, author_id)" +
                    " values (?, ?)");
            statement2.setString(1, book.getTitle());
            statement2.setInt(2, authorId);
            statement2.executeUpdate();
            statement2.close();

            connection.commit();
        } catch (SQLException e) {
            e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
        finally {
            try {
				if (connection != null) { //jesli driver manager nie zadziala (np.zle haslo)
					connection.close();		//to tutaj connection bedzie nullem
				}
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

		refresh();
	}

	@Override
	public void update(Book book) {
		//TODO modyfikacja książki
		//w argumencie book mam juz obiekt, ktory zawiera ksiazke o zadanym id, wraz z zmienionymi juz polami!
		Connection connection = null;
		try {
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			connection.setAutoCommit(false);



			PreparedStatement statement = connection.prepareStatement("update book b set " +
					"b.title = ? where b.id = ?");
			statement.setString(1, book.getTitle());
			statement.setInt(2, book.getId());

			statement.executeUpdate();
			statement.close();


			PreparedStatement statement2 = connection.prepareStatement("update author a set a.first_name = ?, a.last_name = ? " +
					"where a.id = (select author_id from book where id = ?)");
			statement2.setString(1, book.getAuthorFirstName());
			statement2.setString(2, book.getAuthorLastName());
			statement2.setInt(3, book.getId());

			statement2.executeUpdate();
			statement2.close();

			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (connection != null) { //jesli driver manager nie zadziala (np.zle haslo)
					connection.close();		//to tutaj connection bedzie nullem
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

        refresh();
	}

	@Override
	public void delete(Book book) {
		//TODO usunięcie książki
		Connection connection = null;

		try {
			connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
			connection.setAutoCommit(false);

			PreparedStatement statement = connection.prepareStatement("delete from book_category where book_id = ?");
			statement.setInt(1, book.getId());

			statement.executeUpdate();
			statement.close();

			PreparedStatement statement2 = connection.prepareStatement("delete from book where id = ?");
			statement2.setInt(1, book.getId());

			statement2.executeUpdate();
			statement2.close();

			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		} finally {
			try {
				if (connection != null) { //jesli driver manager nie zadziala (np.zle haslo)
					connection.close();		//to tutaj connection bedzie nullem
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		refresh();
	}

}
