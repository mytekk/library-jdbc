package pl.sda.library.view;

import pl.sda.library.table.model.CrudDataTableModel;
import pl.sda.library.table.model.JdbcDataTableModel;

public class JdbcAppView extends AppView {

	@Override
	protected CrudDataTableModel getDataTableModel() {
		return new JdbcDataTableModel();
	}

}
