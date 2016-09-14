package xal.plugin.mysql;

import xal.tools.coding.json.JSONCoder;
import xal.tools.database.*;

import java.sql.Array;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MySQLDatabaseAdaptor extends DatabaseAdaptor {
	/** Table of cached array descriptors keyed by type. The value class is actually oracle.sql.ArrayDescriptor, but Object is used since the Oracle driver is reflected. */
	final private Map<String,Object> ARRAY_DESCRIPTOR_TABLE;

	/**
	 * Public Constructor
	 */
	public MySQLDatabaseAdaptor() {
		ARRAY_DESCRIPTOR_TABLE = new HashMap<String,Object>();
	}

	
	@Override
	public Array getArray(String type, Connection connection, Object array)
			throws DatabaseException {
		// TODO Auto-generated method stub
//		try {
//			final ArrayDescriptor descriptor = getArrayDescriptor(type, connection);
//			return new ARRAY(descriptor, connection, array);
//		}
//		catch(SQLException exception) {
//			Logger.getLogger("global").log( Level.SEVERE, "Error instantiating an SQL array of type: " + type, exception );
//			throw new DatabaseException("Exception generating an SQL array.", this, exception);
//		}
		
		return null;
	}

	@Override
	public Connection getConnection(String urlSpec, String user, String password)
			throws DatabaseException {
		try {
			Connection connect = DriverManager.getConnection(urlSpec, user, password);
			if ( connect != null ) connect.setAutoCommit( false );
			return connect;
		}
		catch(SQLException exception) {
			Logger.getLogger("global").log( Level.SEVERE, "Error connecting to the database at URL: \"" + urlSpec + "\" as user: " + user , exception );
			throw new DatabaseException("Exception connecting to the database.", this, exception);
		}
	}

	
	/**
	 * Fetch all schemas from the connected database. MySQL adaptor returns catalogs instead of schemas.
	 * @return  list of all schemas in the database
	 * @exception DatabaseException
	 * @throws xal.tools.database.DatabaseException  if the schema fetch fails
	 */
	public List<String> fetchAllSchemas( final Connection connection ) throws DatabaseException {
		try {
			final List<String> schemas = new ArrayList<String>();
			final DatabaseMetaData metaData = connection.getMetaData();
			final ResultSet result = metaData.getCatalogs();

			while ( result.next() ) {
				schemas.add( result.getString( "TABLE_CAT" ) );
			}
			result.close();
			return schemas;
		}
		catch ( SQLException exception ) {
			throw new DatabaseException( "Database exception while fetching schemas.", this, exception );
		}
	}


	/** Get the result set for tables for the specified meta data and schema. MySQL adaptor uses the catalog in place of schema. */
	public ResultSet getTablesResultSet( final DatabaseMetaData metaData, final String schema ) throws SQLException {
		return metaData.getTables( schema, null, null, null );
	}


	/** Get the result set of columns for the specified meta data, schema and table. MySQL adaptor uses the catalog in place of schema. */
	public ResultSet getColumnsResultSet( final DatabaseMetaData metaData, final String schema, final String table ) throws SQLException {
		return metaData.getColumns( schema, null, table, null );
	}


	/** Get the result set of primary keys for the specified meta data, schema and table. MySQL adaptor uses the catalog in place of schema. */
	public ResultSet getPrimaryKeysResultSet( final DatabaseMetaData metaData, final String schema, final String table ) throws SQLException {
		return metaData.getPrimaryKeys( schema, null, table );
	}
	
	
	@Override
	public long insert( final Connection connection, final String tableName, final Map<String, Object> items, final String queryForNextPrimKey ) throws SQLException {
		List<Object> valuesList = new ArrayList<>( items.size() );
		StringBuilder sqlBuilder = new StringBuilder( "INSERT INTO ");
		sqlBuilder.append( tableName );
		sqlBuilder.append( "(" );
		StringBuilder valuesBuilder = new StringBuilder( " VALUES " );
		valuesBuilder.append( "(" );
		Set<Map.Entry<String, Object>> entrySet = items.entrySet();
		Iterator<Entry<String, Object>> iterator = entrySet.iterator();
		for ( Map.Entry<String, Object> entry : entrySet ) {
			iterator.next();
			sqlBuilder.append( entry.getKey() );
			valuesBuilder.append( "?" );
			if ( iterator.hasNext() )  {
				sqlBuilder.append( "," );
				valuesBuilder.append( "," );
			}
			valuesList.add( entry.getValue() );
		}
		
		sqlBuilder.append( ")" );
		valuesBuilder.append( ")" );
		sqlBuilder.append( valuesBuilder.toString() );
		String sql = sqlBuilder.toString();
		PreparedStatement insertStatement = connection.prepareStatement( sql );
		int paramIndex = 1;
		for ( Object value : valuesList ) {
			insertStatement.setObject( paramIndex, value );
			paramIndex ++;
		}
		insertStatement.executeUpdate();
		String query = "SELECT LAST_INSERT_ID()";
		PreparedStatement queryStatement = connection.prepareStatement( query );
		ResultSet record = queryStatement.executeQuery();
		record.next();
		
		long nextPrimKey = record.getLong( 1 );
		
		if ( insertStatement != null ) insertStatement.close();
		record.close();
		
		return nextPrimKey;
	}


	@Override
	public void setArray( final PreparedStatement insertStatement, final int pramIndex, final String type, final Connection connection,
			final Object array ) throws SQLException {
		String values = JSONCoder.defaultEncode( array );
		insertStatement.setString( pramIndex, values );
	}


	@Override
	public Object getArrayValues(ResultSet record, String valueType) throws SQLException {
		String valuesString = record.getString( valueType );
		Object theValues = JSONCoder.defaultDecode( valuesString );
		return theValues;
	}

	
//	/**
//	 * Get the array descriptor for the specified array type
//	 * @param type An SQL array type
//	 * @param connection A database connection
//	 * @return the array descriptor for the array type
//	 * @throws java.sql.SQLException if a database exception is thrown
//	 */
//	private ArrayDescriptor getArrayDescriptor(final String type, final Connection connection) throws SQLException {
//		if ( arrayDescriptorMap.containsKey(type) ) {
//			return (ArrayDescriptor)arrayDescriptorMap.get(type);
//		}
//		else {
//			ArrayDescriptor descriptor = ArrayDescriptor.createDescriptor(type, connection);
//			arrayDescriptorMap.put(type, descriptor);
//			return descriptor;
//		}		
//	}
}
