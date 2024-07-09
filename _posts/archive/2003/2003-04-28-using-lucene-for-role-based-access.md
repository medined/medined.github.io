---
layout: post
title: Using Lucene for Role Based Access Control (or RBAC)
date: '2003-04-28T18:56:00.000-04:00'
author: David Medinets
categories:
- "[[java]]"
- "[[lucene]]"
modified_time: '2003-04-28T18:57:26.000-04:00'
blogger_id: tag:blogger.com,1999:blog-3207985.post-200212974
blogger_orig_url: http://affy.blogspot.com/2003/04/using-lucene-for-role-based-access.md
year: 2003
theme: java
---

This class implements simple Role-based Access Control for any object with a unique key. More information than you care
to know regarding RBAC can be found at http://csrc.nist.gov/rbac/.


The comments at the beginning of the Java code describes the concepts and the main() method in the code shows how the
class can be used. Let me know if any code is unclear or if you see room for improvement.

<pre>
package com.affy.lucene;

import java.io.IOException;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

/** This class implements simple Role-based Access
 * Control for any object with a unique key. More
 * information than you care to know regarding RBAC
 * can be found at http://csrc.nist.gov/rbac/.
 *
 * The concepts behind this class are, hopefully,
 * simple to understand because I've taken pains
 * not to implement any fancy business rules.
 *
 * The class understands USERs, ROLEs, and OBJECTs.
 * Each has its own Lucene directory so that searches
 * are as fast as possible. In fact, I recommend
 * that the USER and ROLE directory be RAM-based. While
 * this does require them to be recreated when an
 * application is initialized, it makes the most
 * frequent searches as fast as possible.
 *
 * The OBJECT directory expects that each document
 * has a single unique field called "key". OBJECT
 * documents can also have "owner" and "role" fields.
 * The "owner" field implies that the value of the
 * field represents the entity that produced the
 * document. Documents can have more than one "owner"
 * field. The "role" field represents the roles that
 * are allowed to view the document. Documents can
 * have more than one "role" field.
 *
 * This class is intended to be used in the following way:
 *   Initialize the lucene directories.
 *   Register the Users.
 *   Register the Roles.
 *   Add Users to Roles.
 *   Build Document Store
 *     Document should have key, owner, and role fields.
 *   Query as needed.
 *
 * The main() method provides an example how the class
 * can be used.
 *
 * @author David Medinets [medined@mtolive.com]
 * @created 2003-May-01
 */
public class RBAC {

	/** Every record in the RBAC system has
	 * one of these Fields so that they can be
	 * found via a search.
	 */
	static final Field fieldToFindAllRecords = Field.Keyword("~~", "~");

	/** The following Term is used when searching
	 * for *all* records.
	 */
	static final Term termToFindAllRecords = new Term("~~", "~");

	/** This attribute indicates who is trying
	 * to perform the search.
	 */
	private String accessorId;

	/** The users directory is used to validate
	 * users.
	 */
	private Directory userDirectory;

	/** The roles directory is used to validate
	 * roles.
	 */
	private Directory roleDirectory;

	/** The object directory is used to hold
	 * the objects being protected.
	 */
	private Directory objectDirectory;

	/** This variable holds the analyzer so that
	 * all methods use the same one.
	 */
	StandardAnalyzer analyzer = new StandardAnalyzer();

	/** A do-nothing, no parameter constructor. */
	public RBAC() {
	}

	/** This exception is thrown when the runQuery method is
	 * called but no AccessUserId is specified (ie, it is
	 * null or empty).
	 */
	public class AccessingUserIdNotSpecifiedException extends Exception {
		public AccessingUserIdNotSpecifiedException() {
			super();
		}
		public AccessingUserIdNotSpecifiedException(String msg) {
			super(msg);
		}
	}

	public class UserIdNotUniqueException extends Exception {
		public UserIdNotUniqueException() {
			super();
		}
		public UserIdNotUniqueException(String msg) {
			super(msg);
		}
	}

	public class UserOwnsDocumentException extends Exception {
		public UserOwnsDocumentException() {
			super();
		}
		public UserOwnsDocumentException(String msg) {
			super(msg);
		}
	}

	public class RoleHasUsersException extends Exception {
		public RoleHasUsersException() {
			super();
		}
		public RoleHasUsersException(String msg) {
			super(msg);
		}
	}

	public class UserIdNotFoundException extends Exception {
		public UserIdNotFoundException() {
			super();
		}
		public UserIdNotFoundException(String msg) {
			super(msg);
		}
	}

	public class UserDirectoryNotOpenException extends Exception {
		public UserDirectoryNotOpenException() {
			super();
		}
		public UserDirectoryNotOpenException(String msg) {
			super(msg);
		}
	}

	public class RoleIdNotUniqueException extends Exception {
		public RoleIdNotUniqueException() {
			super();
		}
		public RoleIdNotUniqueException(String msg) {
			super(msg);
		}
	}

	public class RoleIdNotFoundException extends Exception {
		public RoleIdNotFoundException() {
			super();
		}
		public RoleIdNotFoundException(String msg) {
			super(msg);
		}
	}

	public class UserDirectoryAlreadyOpenException extends Exception {
		public UserDirectoryAlreadyOpenException() {
			super();
		}
		public UserDirectoryAlreadyOpenException(String msg) {
			super(msg);
		}
	}

	public class RoleDirectoryAlreadyOpenException extends Exception {
		public RoleDirectoryAlreadyOpenException() {
			super();
		}
		public RoleDirectoryAlreadyOpenException(String msg) {
			super(msg);
		}
	}

	//////////
	// USER Methods
	//////////
	IndexWriter userWriter = null;
	boolean shouldUserIndexBeCreated = true;

	public void openUserDirectory()
		throws IOException, UserDirectoryAlreadyOpenException {
		if (userWriter != null)
			throw new UserDirectoryAlreadyOpenException();
		userWriter =
			new IndexWriter(
				getUserDirectory(),
				analyzer,
				shouldUserIndexBeCreated);
		if (shouldUserIndexBeCreated == true)
			shouldUserIndexBeCreated = false;
	}

	public void closeUserDirectory() throws IOException {
		userWriter.close();
		userWriter = null;
	}

	public void registerUser(String userId)
		throws
			UserDirectoryNotOpenException,
			IOException,
			UserDirectoryAlreadyOpenException {
		boolean isDirectoryUnderLocalControl = false;

		if (userWriter == null) {
			isDirectoryUnderLocalControl = true;
			openUserDirectory();
		}
		Document d = new Document();
		d.add(Field.Keyword("key", userId.toUpperCase().trim()));
		d.add(fieldToFindAllRecords);
		userWriter.addDocument(d);
		if (isDirectoryUnderLocalControl == true) {
			closeUserDirectory();
		}
	}

	public void registerUser(Document d)
		throws
			UserDirectoryNotOpenException,
			IOException,
			UserDirectoryAlreadyOpenException {
		boolean isDirectoryUnderLocalControl = false;

		if (userWriter == null) {
			isDirectoryUnderLocalControl = true;
			openUserDirectory();
		}
		userWriter.addDocument(d);
		if (isDirectoryUnderLocalControl == true) {
			closeUserDirectory();
		}
	}

	public Document findUser(String userId)
		throws IOException, UserIdNotUniqueException {
		IndexSearcher searcher = new IndexSearcher(getUserDirectory());
		TermQuery query =
			new TermQuery(new Term("key", userId.toUpperCase().trim()));
		Hits hits = searcher.search(query);
		int numHits = hits.length();
		switch (hits.length()) {
			case 1 :
				return hits.doc(0);
			case 0 :
				return null;
			default :
				throw new UserIdNotUniqueException();
		}
	}

	/** This method determines if a user owns any
	 * documents in the object directory.
	 *
	 * @param userId The user who potentially owns documents.
	 * @return true, if the user owns documents.
	 * @throws IOException
	 */
	public boolean userOwnsDocuments(String userId) throws IOException {
		boolean rv = false;
		if (IndexReader.indexExists(getObjectDirectory()) == true) {
			IndexSearcher searcher =
				new IndexSearcher(getObjectDirectory());
			TermQuery query = new TermQuery(new Term("owner", userId));
			Hits hits = searcher.search(query);
			searcher.close();
			rv = hits.length() > 0;
		}
		return rv;
	}

	/** This method determines if a user has any roles
	 * in the role directory. This method returns a Hits
	 * object so that the calling method can remove the
	 * user from the roles, if needed. For example, if the
	 * user were being dropped.
	 *
	 * @param userId The user who potentially has roles.
	 * @return null, if no roles. Otherwise, the Hits that were found.
	 * @throws IOException
	 */
	public Hits userHasRoles(String userId) throws IOException {
		Hits hits = null;
		if (IndexReader.indexExists(getRoleDirectory()) == true) {
			IndexSearcher searcher = new IndexSearcher(getRoleDirectory());
			TermQuery query = new TermQuery(new Term("user", userId));
			hits = searcher.search(query);
			searcher.close();
		}
		return hits;
	}

	/** This method deregisters a user from the userDirectory
	 * if the user has no associated documents.
	 *
	 * @param userId The user to be removed.
	 * @throws IOException
	 * @throws UserOwnsDocumentException Thrown when the user has documents
	 * in the system.
	 */
	public void dropUser(String userId)
		throws
			UserOwnsDocumentException,
			RoleDirectoryAlreadyOpenException,
			IOException,
			RoleIdNotUniqueException,
			RoleIdNotFoundException {
		if (IndexReader.indexExists(getUserDirectory()) == true) {
			if (userOwnsDocuments(userId) == true) {
				throw new UserOwnsDocumentException();
			}
			Hits hits = userHasRoles(userId);
			if (hits != null) {
				// remove the user from their roles.
				int _length = hits.length();
				for (int i = 0; i < _length; i++) {
					unlinkUserFromRole(userId, hits.doc(i));
				}
			}
			IndexReader reader = IndexReader.open(getUserDirectory());
			reader.delete(new Term("key", userId.toUpperCase().trim()));
			reader.close();
		}
	}

	public void listUsers() throws IOException {
		boolean isHeaderDisplayed = false;
		TermQuery query = new TermQuery(termToFindAllRecords);
		IndexSearcher searcher = new IndexSearcher(getUserDirectory());
		Hits hits = searcher.search(query);
		int _length = hits.length();
		System.out.println("HITS: " + _length);
		if (isHeaderDisplayed == false) {
			isHeaderDisplayed = true;
			System.out.println("Registered Users");
			System.out.println("----------------");
		}
		for (int i = 0; i < _length; i++) {
			System.out.println("  doc: " + hits.doc(i));
		}
		searcher.close();
		System.out.println("");
	}

	//////////
	// ROLE Methods
	//////////
	IndexWriter roleWriter = null;
	boolean shouldRoleIndexBeCreated = true;

	public void openRoleDirectory()
		throws IOException, RoleDirectoryAlreadyOpenException {
		if (roleWriter != null)
			throw new RoleDirectoryAlreadyOpenException();
		roleWriter =
			new IndexWriter(
				getRoleDirectory(),
				analyzer,
				shouldRoleIndexBeCreated);
		if (shouldRoleIndexBeCreated == true)
			shouldRoleIndexBeCreated = false;
	}

	public void closeRoleDirectory() throws IOException {
		roleWriter.close();
		roleWriter = null;
	}

	public void registerRole(String roleId)
		throws IOException, RoleDirectoryAlreadyOpenException {
		boolean isDirectoryUnderLocalControl = false;

		if (roleWriter == null) {
			isDirectoryUnderLocalControl = true;
			openRoleDirectory();
		}
		Document d = new Document();
		d.add(Field.Keyword("key", roleId.toUpperCase().trim()));
		d.add(fieldToFindAllRecords);
		roleWriter.addDocument(d);

		if (isDirectoryUnderLocalControl == true) {
			closeRoleDirectory();
		}
	}

	public void registerRole(Document d)
		throws IOException, RoleDirectoryAlreadyOpenException {
		boolean isDirectoryUnderLocalControl = false;

		if (roleWriter == null) {
			isDirectoryUnderLocalControl = true;
			openRoleDirectory();
		}
		roleWriter.addDocument(d);
		if (isDirectoryUnderLocalControl == true) {
			closeRoleDirectory();
		}
	}

	public Document findRole(final String roleId)
		throws IOException, RoleIdNotUniqueException, RoleIdNotFoundException {
		IndexSearcher searcher = new IndexSearcher(getRoleDirectory());
		TermQuery query =
			new TermQuery(new Term("key", roleId.toUpperCase().trim()));
		Hits hits = searcher.search(query);
		searcher.close();
		int numHits = hits.length();
		switch (hits.length()) {
			case 1 :
				return hits.doc(0);
			case 0 :
				throw new RoleIdNotFoundException();
			default :
				throw new RoleIdNotUniqueException();
		}
	}

	/** This method determines if a role has users associated
	 * with it.
	 * @param roleId The role to check.
	 * @return true, if the role has associated users.
	 * @throws IOException
	 * @throws RoleIdNotUniqueException
	 * @throws RoleIdNotFoundException
	 */
	public boolean roleHasUsers(final String roleId)
		throws IOException, RoleIdNotUniqueException, RoleIdNotFoundException {
		Document roleDoc = findRole(roleId);
		Field[] rolePlayers = roleDoc.getFields("user");
		return rolePlayers != null && rolePlayers.length > 0;
	}

	/** This method removes a role from the role directory.
	 *
	 * @param roleId The role to be removed.
	 * @throws IOException
	 * @throws RoleIdNotUniqueException
	 * @throws RoleIdNotFoundException
	 * @throws RoleHasUsersException
	 */
	public void dropRole(String roleId)
		throws
			IOException,
			RoleIdNotUniqueException,
			RoleIdNotFoundException,
			RoleHasUsersException {
		if (IndexReader.indexExists(getRoleDirectory()) == true) {
			if (roleHasUsers(roleId) == true)
				throw new RoleHasUsersException(roleId);
			IndexReader reader = IndexReader.open(getRoleDirectory());
			reader.delete(new Term("key", roleId.toUpperCase().trim()));
			reader.close();
		}
	}

	public void listRoles() throws IOException {
		boolean isHeaderDisplayed = false;
		TermQuery query = new TermQuery(termToFindAllRecords);
		IndexSearcher searcher = new IndexSearcher(getRoleDirectory());
		Hits hits = searcher.search(query);
		int _length = hits.length();
		System.out.println("HITS: " + _length);
		if (isHeaderDisplayed == false) {
			isHeaderDisplayed = true;
			System.out.println("Registered Roles");
			System.out.println("----------------");
		}
		for (int i = 0; i < _length; i++) {
			System.out.println("  doc: " + hits.doc(i));
		}
		searcher.close();
		System.out.println("");
	}

	//////////
	// USER-ROLE Methods
	//////////
	// NOTE: Ignoring the issue of duplicate assignments.
	public void linkRoleToUser(String userId, String roleId)
		throws
			UserIdNotUniqueException,
			UserIdNotFoundException,
			UserDirectoryNotOpenException,
			UserDirectoryAlreadyOpenException,
			RoleDirectoryAlreadyOpenException,
			UserOwnsDocumentException,
			IOException,
			RoleIdNotUniqueException,
			RoleIdNotFoundException {
		Document user = findUser(userId);
		if (user != null) {
			Document role = findRole(roleId);
			if (role != null) {
				// replace the user and role documents.
				dropUser(userId);

				IndexReader reader = IndexReader.open(getRoleDirectory());
				reader.delete(
					new Term("key", roleId.toUpperCase().trim()));
				reader.close();

				// add role to user document
				user.add(
					Field.Keyword("role", roleId.toUpperCase().trim()));
				registerUser(user);

				// add user to role document
				role.add(
					Field.Keyword("user", userId.toUpperCase().trim()));
				registerRole(role);
			}
		}
	}

	/** This method removes a user from a role document.
	 * The role document should look like this:
	 *  Document <
	 *   Keyword<user:ZOEY>
	 *   Keyword<user:MARY>
	 *   Keyword<~~:~>
	 *   Keyword<key:DOCTOR>
	 * >
	 *
	 * @param userId The user to be removed.
	 * @param roleId The role being changed.
	 */
	public void unlinkUserFromRole(
		final String userId,
		final String roleId)
		throws
			RoleDirectoryAlreadyOpenException,
			IOException,
			RoleIdNotUniqueException,
			RoleIdNotFoundException,
			RoleHasUsersException {
		unlinkUserFromRole(userId, findRole(roleId));
	}

	/** This method removes a user from a role document.
	 * The role document should look like this:
	 *  Document <
	 *   Keyword<user:ZOEY>
	 *   Keyword<user:MARY>
	 *   Keyword<~~:~>
	 *   Keyword<key:DOCTOR>
	 * >
	 *
	 * @param userId The user to be removed.
	 * @param doc The role document.
	 */
	public void unlinkUserFromRole(
		final String userId,
		final Document doc)
		throws
			RoleDirectoryAlreadyOpenException,
			IOException,
			RoleIdNotUniqueException,
			RoleIdNotFoundException {
		String roleId = doc.getField("key").stringValue();
		Field[] rolePlayers = doc.getFields("user");
		if (rolePlayers == null) {
			// there are no users in this role, therefore
			// the user doesn't need to be removed.
		} else {
			// build a new role document.
			Document newDoc = new Document();
			newDoc.add(Field.Keyword("key", roleId));
			newDoc.add(fieldToFindAllRecords);
			for (int i = 0; i < rolePlayers.length; i++) {
				String rolePlayer = rolePlayers[i].stringValue();
				if (rolePlayer.equals(userId) == false)
					newDoc.add(Field.Keyword("user", rolePlayer));
			}
			// drop the role so that it can be replaced with
			// the new role document.
			IndexReader reader = IndexReader.open(getRoleDirectory());
			reader.delete(new Term("key", roleId.toUpperCase().trim()));
			reader.close();

			// add the new role document.
			registerRole(newDoc);
		}
	}

	// TODO write linkRoleToDocument method.
	// TODO write unlinkRoleFromDocument method.

	//////////
	// SEARCH Methods
	//////////
	public Hits runQuery(String accessorId, Query q)
		throws
			AccessingUserIdNotSpecifiedException,
			IOException,
			UserIdNotUniqueException,
			UserIdNotFoundException {
		if (accessorId == null || accessorId.length() < 1)
			throw new AccessingUserIdNotSpecifiedException();

		if (findUser(accessorId) == null)
			throw new UserIdNotFoundException();

		setUserId(accessorId);
		IndexSearcher searcher = new IndexSearcher(getObjectDirectory());
		Hits hits = searcher.search(q, new RBACFilter(this));
		searcher.close();
		return hits;
	}

	//////////
	// Getters and Setters
	//////////

	/**
	 * @return Directory
	 */
	public Directory getUserDirectory() {
		return userDirectory;
	}

	/**
	 * @return String
	 */
	public String getUserId() {
		return accessorId;
	}

	/**
	 * Sets the userDirectory.
	 * @param userDirectory The userDirectory to set
	 */
	public void setUserDirectory(Directory userDirectory) {
		this.userDirectory = userDirectory;
	}

	/**
	 * @return Directory
	 */
	public Directory getRoleDirectory() {
		return roleDirectory;
	}

	/**
	 * Sets the roleDirectory.
	 * @param roleDirectory The roleDirectory to set
	 */
	public void setRoleDirectory(Directory roleDirectory) {
		this.roleDirectory = roleDirectory;
	}

	/**
	 * @return Directory
	 */
	public Directory getObjectDirectory() {
		return objectDirectory;
	}

	/**
	 * Sets the objectDirectory.
	 * @param objectDirectory The objectDirectory to set
	 */
	public void setObjectDirectory(Directory objectDirectory) {
		this.objectDirectory = objectDirectory;
	}

	/**
	 * Sets the userId.
	 * @param userId The userId to set
	 */
	public void setUserId(String userId) {
		this.accessorId = userId;
	}

	private class RBACFilter extends Filter {
		private RBAC rbac;

		public RBACFilter(RBAC rbac) {
			this.rbac = rbac;
		}

		public BitSet bits(IndexReader reader) throws IOException {
			BitSet bits = new BitSet(reader.maxDoc());

			// find the roles for this user.
			try {
				Vector searchClauses = new Vector();
				searchClauses.add(
					new TermQuery(new Term("owner", rbac.getUserId())));
				Document user = rbac.findUser(rbac.getUserId());
				TermQuery tq =
					new TermQuery(new Term("key", rbac.getUserId()));
				IndexSearcher searcher =
					new IndexSearcher(rbac.getUserDirectory());
				Hits hits = searcher.search(tq);
				int numHits = hits.length();
				for (int i = 0; i < numHits; i++) {
					Field[] a = hits.doc(i).getFields("role");
					if (a != null) {
					  for (i = 0; i < a.length; i++) {
					    searchClauses.add(
					      new TermQuery(
					        new Term("role", a[i].stringValue())));
					  }
					}
				}
				searcher.close();

				Enumeration e = searchClauses.elements();
				BooleanQuery bq = new BooleanQuery();
				while (e.hasMoreElements()) {
					tq = (TermQuery) e.nextElement();
					bq.add(tq, false, false);
				}
				searcher = new IndexSearcher(rbac.getObjectDirectory());
				hits = searcher.search(bq);
				numHits = hits.length();
				for (int i = 0; i < numHits; i++) {
					bits.set(hits.id(i));
				}
				searcher.close();
			} catch (RBAC.UserIdNotUniqueException e) {
				// I suppose the following is a bit sacrilegous
				// because I am subverting an IOException into
				// a generic RuntimeException but I don't know
				// another way to handle this situation.
				throw new RuntimeException(
					"UserId [" + rbac.getUserId() + "] not Unique.");
			}

			return bits;
		}
	}

	public static void main(String[] args)
		throws
			AccessingUserIdNotSpecifiedException,
			UserIdNotUniqueException,
			UserIdNotFoundException,
			UserDirectoryNotOpenException,
			UserDirectoryAlreadyOpenException,
			RoleDirectoryAlreadyOpenException,
			UserOwnsDocumentException,
			IOException,
			RoleIdNotUniqueException,
			RoleIdNotFoundException,
			RoleHasUsersException {
		RAMDirectory indexStore = new RAMDirectory();

		// initialize the lucene directories.
		RBAC rbac = new RBAC();
		rbac.setUserDirectory(new RAMDirectory());
		rbac.setRoleDirectory(new RAMDirectory());
		rbac.setObjectDirectory(indexStore);

		// register the Users.
		rbac.registerUser("DAVID");
		rbac.registerUser("JOHN");
		rbac.registerUser("MARY");
		rbac.registerUser("RICK");
		rbac.registerUser("ZOEY");

		// register the Roles.
		rbac.registerRole("DOCTOR");
		rbac.registerRole("NURSE");
		rbac.registerRole("ADMIN");
		rbac.registerRole("PATIENT");

		// Add Users to Roles.
		rbac.linkRoleToUser("MARY", "DOCTOR");
		rbac.linkRoleToUser("ZOEY", "DOCTOR");
		rbac.linkRoleToUser("JOHN", "NURSE");
		rbac.linkRoleToUser("RICK", "ADMIN");

		System.out.println("Doctor: " + rbac.roleHasUsers("DOCTOR"));
		System.out.println("Patient: " + rbac.roleHasUsers("PATIENT"));

		//rbac.listRoles();
		//rbac.listUsers();

		// build document store.
		StandardAnalyzer analyzer = new StandardAnalyzer();
		IndexWriter writer = new IndexWriter(indexStore, analyzer, true);

		// an unowned document.
		Document d = new Document();
		d.add(Field.Keyword("body", "unowned01"));
		writer.addDocument(d);

		d = new Document();
		d.add(Field.Keyword("body", "unowned02"));
		writer.addDocument(d);

		d = new Document();
		d.add(Field.Keyword("body", "AAA"));
		d.add(Field.Keyword("owner", "DAVID"));
		d.add(Field.Keyword("role", "DOCTOR"));
		d.add(Field.Keyword("role", "NURSE"));
		writer.addDocument(d);

		d = new Document();
		d.add(Field.Keyword("body", "BBB"));
		d.add(Field.Keyword("owner", "JOHN"));
		d.add(Field.Keyword("role", "ADMIN"));
		writer.addDocument(d);

		writer.close();

		{
			String ownerId = "DAVID";
			String accessorId = "DAVID";

			System.out.println("");
			System.out.println(
				"All Documents Owned by "
					+ ownerId
					+ " Viewable by "
					+ accessorId);
			TermQuery query = new TermQuery(new Term("owner", ownerId));

			Hits hits = rbac.runQuery(accessorId, query);

			int _length = hits.length();
			System.out.println("HITS: " + _length);
			for (int i = 0; i < _length; i++) {
				Document doc = hits.doc(i);
				System.out.println("  doc: " + doc);
				Field field = doc.getField("body");
				System.out.println("  value: " + field.stringValue());
			}
		}
		{
			String ownerId = "JOHN";
			String accessorId = "DAVID";

			System.out.println("");
			System.out.println(
				"All Documents Owned by "
					+ ownerId
					+ " Viewable by "
					+ accessorId);
			TermQuery query = new TermQuery(new Term("owner", ownerId));

			Hits hits = rbac.runQuery(accessorId, query);

			int _length = hits.length();
			System.out.println("HITS: " + _length);
			for (int i = 0; i < _length; i++) {
				Document doc = hits.doc(i);
				System.out.println("  doc: " + doc);
				Field field = doc.getField("body");
				System.out.println("  value: " + field.stringValue());
			}
		}

		System.out.println("");
		System.out.println("Done.");
	}

}
</pre>