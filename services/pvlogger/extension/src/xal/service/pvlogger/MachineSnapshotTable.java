//
//  MachineSnapshotTable.java
//  xal
//
//  Created by Pelaia II, Tom on 10/10/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.service.pvlogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

import javafx.scene.chart.PieChart.Data;

import java.util.ArrayList;
import java.util.HashMap;

import xal.tools.database.DatabaseAdaptor;



/** represent the machine snapshot database table */
class MachineSnapshotTable {
	/** database table name */
	protected final String TABLE_NAME;

	/** Machine snapshot Primary Key column */
	protected final String PRIMARY_KEY;

	/** time stamp column */
	protected final String TIMESTAMP_COLUMN;

	/** type column */
	protected final String TYPE_COLUMN;

	/** comment column */
	protected final String COMMENT_COLUMN;

	/** SQL to get the next primary key */
	protected final String NEXT_PRIMARY_KEY_SQL;
	
	/**database adaptor*/
	protected  DatabaseAdaptor databaseAdaptor; 


	/** Constructor */
	public MachineSnapshotTable( final DBTableConfiguration configuration ) {
		TABLE_NAME = configuration.getTableName();

		PRIMARY_KEY = configuration.getColumn( "primaryKey" );
		TIMESTAMP_COLUMN = configuration.getColumn( "timestamp" );
		TYPE_COLUMN = configuration.getColumn( "type" );
		COMMENT_COLUMN = configuration.getColumn( "comment" );

		NEXT_PRIMARY_KEY_SQL = configuration.getQuerySQL( "nextPrimaryKey" );
	}


	/** insert the machine snapshot and update its ID upone success */
	public void insert( final Connection connection, final ChannelSnapshotTable channelSnapshotTable, final MachineSnapshot machineSnapshot ) throws SQLException {
		
		final Map<String, Object> items = new HashMap<>();
		
		final String type = machineSnapshot.getType();
		items.put( TYPE_COLUMN, type );
		final Timestamp timeStamp = new Timestamp( machineSnapshot.getTimestamp().getTime() );
		items.put( TIMESTAMP_COLUMN, timeStamp );
		items.put( COMMENT_COLUMN,  machineSnapshot.getComment() );
		
		long primaryKey = databaseAdaptor.insert( connection, TABLE_NAME, items, NEXT_PRIMARY_KEY_SQL );
		
		final ChannelSnapshot[] channelSnapshots = machineSnapshot.getChannelSnapshots();
		channelSnapshotTable.insert( connection, databaseAdaptor, channelSnapshots, primaryKey );

		connection.commit();
		machineSnapshot.setId( primaryKey );
	}


	/**
	 * Fetch a machine snaspshot with the specified primary key.
	 * @param connection database connection
	 * @param primaryKey The unique machine snapshot identifier
	 * @return The machine snapshop read from the persistent store.
	 */
	public MachineSnapshot fetchMachineSnapshot( final Connection connection, final long primaryKey ) throws SQLException {
		final PreparedStatement queryStatement  = getQueryByPrimaryKeyStatement( connection );
		queryStatement.setLong( 1, primaryKey );
		final ResultSet record = queryStatement.executeQuery();
		if ( record.next() ) {
			final String type = record.getString( TYPE_COLUMN );
			final Timestamp timestamp = record.getTimestamp( TIMESTAMP_COLUMN );
			final String comment = record.getString( COMMENT_COLUMN );
			if(queryStatement != null) {
				queryStatement.close();
			}
			record.close();
			return new MachineSnapshot( primaryKey, type, timestamp, comment, new ChannelSnapshot[0] );
		}
		else {
			if(queryStatement != null) {
				queryStatement.close();
			}
			record.close();
			return null;
		}
	}


	/**
	 * Fetch the channel snapshots from the data source and populate the machine snapshot
	 * @param connection database connection
	 * @param channelSnapshotTable table proxy of channel snapshots
	 * @param machineSnapshot The machine snapshot for which to fetch the channel snapshots and load them
	 * @return the machineSnapshot which is the same as the parameter returned for convenience
	 */
	public MachineSnapshot loadChannelSnapshotsInto( final Connection connection, final ChannelSnapshotTable channelSnapshotTable, final MachineSnapshot machineSnapshot ) throws SQLException {
		final ChannelSnapshot[] snapshots = channelSnapshotTable.fetchChannelSnapshotsForMachineSnapshotID( connection, databaseAdaptor,  machineSnapshot.getId() );
		machineSnapshot.setChannelSnapshots( snapshots );
		return machineSnapshot;
	}
	
	/**set the database adaptor*/
	public void setDatabaseAdaptor( final DatabaseAdaptor databaseAdaptor ) {
		this.databaseAdaptor = databaseAdaptor;
	}

	/**
	 * Fetch the machine snapshots within the specified time range. If the type is not null, then restrict the machine snapshots to those of the specified type.
	 * The machine snapshots do not include the channel snapshots. A complete snapshot can be obtained using the fetchMachineSnapshot(id) method.
	 * @param connection database connection
	 * @param type The type of machine snapshots to fetch or null for no restriction
	 * @param startTime The start time of the time range
	 * @param endTime The end time of the time range
	 * @return An array of machine snapshots meeting the specified criteria
	 */
	public MachineSnapshot[] fetchMachineSnapshotsInRange( final Connection connection, final String type, final java.util.Date startTime, final java.util.Date endTime ) throws SQLException {
		if ( type == null ) {
			return fetchMachineSnapshotsInRange( connection, startTime, endTime );
		}

		final List<MachineSnapshot> snapshots = new ArrayList<MachineSnapshot>();

		final PreparedStatement queryStatement = getQueryByTypeAndTimerangeStatement( connection );
		queryStatement.setString( 1, type );
		queryStatement.setTimestamp( 2, new Timestamp( startTime.getTime() ) );
		queryStatement.setTimestamp( 3, new Timestamp( endTime.getTime() ) );

		final ResultSet snapshotResult = queryStatement.executeQuery();
		while ( snapshotResult.next() ) {
			final long id = snapshotResult.getLong( PRIMARY_KEY );
			final String foundType = snapshotResult.getString( TYPE_COLUMN );
			final Timestamp timestamp = snapshotResult.getTimestamp( TIMESTAMP_COLUMN	);
			final String comment = snapshotResult.getString( COMMENT_COLUMN );
			snapshots.add( new MachineSnapshot( id, foundType, timestamp, comment, new ChannelSnapshot[0] ) );
		}
		if(queryStatement != null) {
			queryStatement.close();
		}
		snapshotResult.close();
		return snapshots.toArray( new MachineSnapshot[snapshots.size()] );
	}


	/**
	 * Fetch the machine snapshots within the specified time range. The machine snapshots do not include the channel snapshots. A complete snapshot
	 * can be obtained using the fetchMachineSnapshot(id) method.
	 * @param connection database connection
	 * @param startTime The start time of the time range
	 * @param endTime The end time of the time range
	 * @return An array of machine snapshots meeting the specified criteria
	 */
	protected MachineSnapshot[] fetchMachineSnapshotsInRange( final Connection connection, final java.util.Date startTime, final java.util.Date endTime ) throws SQLException {
		final List<MachineSnapshot> snapshots = new ArrayList<MachineSnapshot>();

		final PreparedStatement queryStatement = getQueryByTimerangeStatement( connection );
		queryStatement.setTimestamp( 1, new Timestamp( startTime.getTime() ) );
		queryStatement.setTimestamp( 2, new Timestamp( endTime.getTime() ) );

		final ResultSet snapshotResult = queryStatement.executeQuery();
		while ( snapshotResult.next() ) {
			final long id = snapshotResult.getLong( PRIMARY_KEY );
			final String foundType = snapshotResult.getString( TYPE_COLUMN );
			final Timestamp timestamp = snapshotResult.getTimestamp( TIMESTAMP_COLUMN	);
			final String comment = snapshotResult.getString( COMMENT_COLUMN );
			snapshots.add( new MachineSnapshot( id, foundType, timestamp, comment, new ChannelSnapshot[0] ) );
		}
		if(queryStatement != null) {
			queryStatement.close();
		}
		snapshotResult.close();
		return snapshots.toArray( new MachineSnapshot[snapshots.size()] );
	}


	/**
	 * Create the prepared statement if it does not already exist.
	 * @return the prepared statement to query for all machine snapshots
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getQueryStatement( final Connection connection ) throws SQLException {
		return connection.prepareStatement( "SELECT * FROM " + TABLE_NAME );
	}


	/**
	 * Create the prepared statement if it does not already exist.
	 * @return the prepared statement to query for machine snapshots by primary key
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getQueryByPrimaryKeyStatement( final Connection connection ) throws SQLException {
			return connection.prepareStatement( "SELECT * FROM " + TABLE_NAME + " WHERE " + PRIMARY_KEY + " = ?" );
	}


	/**
	 * Create the prepared statement if it does not already exist.
	 * @return the prepared statement to query for machine snapshots by type and time range
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getQueryByTypeAndTimerangeStatement( final Connection connection ) throws SQLException {
			return connection.prepareStatement( "SELECT * FROM " + TABLE_NAME + " WHERE " + TYPE_COLUMN + " = ? AND " + TIMESTAMP_COLUMN + " > ? AND " + TIMESTAMP_COLUMN + " < ? order by " + TIMESTAMP_COLUMN );
	}


	/**
	 * Create the prepared statement if it does not already exist.
	 * @return the prepared statement to query for machine snapshots by time range
	 * @throws java.sql.SQLException  if an exception occurs during a SQL evaluation
	 */
	protected PreparedStatement getQueryByTimerangeStatement( final Connection connection ) throws SQLException {
			return connection.prepareStatement( "SELECT * FROM " + TABLE_NAME + " WHERE " + TIMESTAMP_COLUMN + " > ? AND " + TIMESTAMP_COLUMN + " < ? order by " + TIMESTAMP_COLUMN );
	}
}
