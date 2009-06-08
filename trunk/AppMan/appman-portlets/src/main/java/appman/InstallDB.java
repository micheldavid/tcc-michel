package appman;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe para instalação da base de dados.
 * Exemplo de URL para o HSQL do uPortal: jdbc:hsqldb:hsql://localhost:8887
 * @author michel
 *
 */
public class InstallDB {

	public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {
		new InstallDB(args);
	}

	private InstallDB(String[] args) throws ClassNotFoundException, SQLException, IOException {
		Class.forName("org.hsqldb.jdbcDriver");
		Connection conn = DriverManager.getConnection(args[1], args[2], args[3]);

		BufferedReader reader = new BufferedReader(new FileReader(args[0]));
		for (String line; (line = reader.readLine()) != null;) {
			if (line.length() == 0) continue;

			Statement stmt = conn.createStatement();
			try {
				stmt.execute(line);
			} catch (SQLException ex) {
				System.out.println(ex.getMessage());
			}
			stmt.close();
		}
		conn.close();
	}
}
