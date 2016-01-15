package database;

/**
 * @author y-yusuke
 *
 */
public class Query {
	public Query() {
	}

	public String delete(int i) {
		String sql_delete = "select CODE_FRAGMENT_GENEALOGY_ELEMENT.CODE_FRAGMENT_ID,"
				+ "CODE_FRAGMENT_GENEALOGY_ELEMENT.CODE_FRAGMENT_GENEALOGY_ID,"
				+ "CRD.TYPE,"
				+ "REPOSITORY.REPOSITORY_ROOT_URL,"
				+ "FILE.FILE_PATH,"
				+ "CODE_FRAGMENT.START_LINE,"
				+ "CODE_FRAGMENT.END_LINE,"
				+ "REVISION.REVISION_IDENTIFIER,"
				+ "CODE_FRAGMENT_GENEALOGY.START_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT_GENEALOGY.END_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT.START_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT.END_COMBINED_REVISION_ID "
				+ "from (((((("
					+ "CODE_FRAGMENT_GENEALOGY INNER JOIN CODE_FRAGMENT_GENEALOGY_ELEMENT"
					+ " ON CODE_FRAGMENT_GENEALOGY.CODE_FRAGMENT_GENEALOGY_ID = CODE_FRAGMENT_GENEALOGY_ELEMENT.CODE_FRAGMENT_GENEALOGY_ID"
					+ ")INNER JOIN CODE_FRAGMENT"
					+ " ON CODE_FRAGMENT_GENEALOGY_ELEMENT.CODE_FRAGMENT_ID = CODE_FRAGMENT.CODE_FRAGMENT_ID"
					+ ")INNER JOIN CRD"
					+ " ON CODE_FRAGMENT.CRD_ID = CRD.CRD_ID"
					+ ")INNER JOIN FILE"
					+ " ON CODE_FRAGMENT.OWNER_FILE_ID = FILE.FILE_ID"
					+ ")INNER JOIN REPOSITORY"
					+ " ON CODE_FRAGMENT.OWNER_REPOSITORY_ID = REPOSITORY.REPOSITORY_ID"
					+ ")INNER JOIN REVISION"
					+ " ON CODE_FRAGMENT.END_COMBINED_REVISION_ID = REVISION.REVISION_ID"
				+ ")"
				+ "where CODE_FRAGMENT_GENEALOGY.END_COMBINED_REVISION_ID = "
				+ i + " AND CODE_FRAGMENT.END_COMBINED_REVISION_ID = " + i
				+" AND (CODE_FRAGMENT.END_LINE - CODE_FRAGMENT.START_LINE + 1) > 5";
		return sql_delete;
	}

	public String add(int i) {
		String sql_add = "select CODE_FRAGMENT_GENEALOGY_ELEMENT.CODE_FRAGMENT_ID,"
				+ "CODE_FRAGMENT_GENEALOGY_ELEMENT.CODE_FRAGMENT_GENEALOGY_ID,"
				+ "CRD.TYPE,"
				+ "REPOSITORY.REPOSITORY_ROOT_URL,"
				+ "FILE.FILE_PATH,"
				+ "CODE_FRAGMENT.START_LINE,"
				+ "CODE_FRAGMENT.END_LINE,"
				+ "REVISION.REVISION_IDENTIFIER,"
				+ "CODE_FRAGMENT_GENEALOGY.START_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT_GENEALOGY.END_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT.START_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT.END_COMBINED_REVISION_ID "
				+ "from (((((("
					+ "CODE_FRAGMENT_GENEALOGY INNER JOIN CODE_FRAGMENT_GENEALOGY_ELEMENT"
					+ " ON CODE_FRAGMENT_GENEALOGY.CODE_FRAGMENT_GENEALOGY_ID = CODE_FRAGMENT_GENEALOGY_ELEMENT.CODE_FRAGMENT_GENEALOGY_ID"
					+ ")INNER JOIN CODE_FRAGMENT"
					+ " ON CODE_FRAGMENT_GENEALOGY_ELEMENT.CODE_FRAGMENT_ID = CODE_FRAGMENT.CODE_FRAGMENT_ID"
					+ ")INNER JOIN CRD"
					+ " ON CODE_FRAGMENT.CRD_ID = CRD.CRD_ID"
					+ ")INNER JOIN FILE"
					+ " ON CODE_FRAGMENT.OWNER_FILE_ID = FILE.FILE_ID"
					+ ")INNER JOIN REPOSITORY"
					+ " ON CODE_FRAGMENT.OWNER_REPOSITORY_ID = REPOSITORY.REPOSITORY_ID"
					+ ")INNER JOIN REVISION"
					+ " ON CODE_FRAGMENT.START_COMBINED_REVISION_ID = REVISION.REVISION_ID"
				+ ")"
				+ "where CODE_FRAGMENT_GENEALOGY.START_COMBINED_REVISION_ID = "
				+ i + 1 + " AND CODE_FRAGMENT.START_COMBINED_REVISION_ID = " + i + 1
				+" AND (CODE_FRAGMENT.END_LINE - CODE_FRAGMENT.START_LINE + 1) > 5";
		return sql_add;
	}

	public String before_fix(int i) {
		String sql_before_fix = "select CODE_FRAGMENT.CODE_FRAGMENT_ID,"
				+ "CODE_FRAGMENT_GENEALOGY_LINK_ELEMENT.CODE_FRAGMENT_GENEALOGY_ID,"
				+ "CRD.TYPE,"
				+ "REPOSITORY.REPOSITORY_ROOT_URL,"
				+ "FILE.FILE_PATH,"
				+ "CODE_FRAGMENT.START_LINE,"
				+ "CODE_FRAGMENT.END_LINE,"
				+ "REVISION.REVISION_IDENTIFIER,"
				+ "CODE_FRAGMENT_LINK.BEFORE_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT_LINK.AFTER_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT.START_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT.END_COMBINED_REVISION_ID "
				+ "from (((((("
					+ "CODE_FRAGMENT_LINK INNER JOIN CODE_FRAGMENT_GENEALOGY_LINK_ELEMENT"
					+ " ON CODE_FRAGMENT_LINK.CODE_FRAGMENT_LINK_ID = CODE_FRAGMENT_GENEALOGY_LINK_ELEMENT.CODE_FRAGMENT_LINK_ID"
					+ ")INNER JOIN CODE_FRAGMENT"
					+ " ON CODE_FRAGMENT_LINK.BEFORE_ELEMENT_ID = CODE_FRAGMENT.CODE_FRAGMENT_ID"
					+ ")INNER JOIN CRD"
					+ " ON CODE_FRAGMENT.CRD_ID = CRD.CRD_ID"
					+ ")INNER JOIN FILE"
					+ " ON CODE_FRAGMENT.OWNER_FILE_ID = FILE.FILE_ID"
					+ ")INNER JOIN REPOSITORY"
					+ " ON CODE_FRAGMENT.OWNER_REPOSITORY_ID = REPOSITORY.REPOSITORY_ID"
					+ ")INNER JOIN REVISION"
					+ " ON CODE_FRAGMENT_LINK.BEFORE_COMBINED_REVISION_ID = REVISION.REVISION_ID"
				+ ")"
				+ "where CODE_FRAGMENT_LINK.BEFORE_COMBINED_REVISION_ID = " + i
				+" AND (CODE_FRAGMENT.END_LINE - CODE_FRAGMENT.START_LINE + 1) > 5";
		return sql_before_fix;
	}

	public String after_fix(int i) {
		String sql_after_fix = "select CODE_FRAGMENT.CODE_FRAGMENT_ID,"
				+ "CODE_FRAGMENT_GENEALOGY_LINK_ELEMENT.CODE_FRAGMENT_GENEALOGY_ID,"
				+ "CRD.TYPE,"
				+ "REPOSITORY.REPOSITORY_ROOT_URL,"
				+ "FILE.FILE_PATH,"
				+ "CODE_FRAGMENT.START_LINE,"
				+ "CODE_FRAGMENT.END_LINE,"
				+ "REVISION.REVISION_IDENTIFIER,"
				+ "CODE_FRAGMENT_LINK.BEFORE_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT_LINK.AFTER_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT.START_COMBINED_REVISION_ID,"
				+ "CODE_FRAGMENT.END_COMBINED_REVISION_ID "
				+ "from (((((("
					+ "CODE_FRAGMENT_LINK INNER JOIN CODE_FRAGMENT_GENEALOGY_LINK_ELEMENT"
					+ " ON CODE_FRAGMENT_LINK.CODE_FRAGMENT_LINK_ID = CODE_FRAGMENT_GENEALOGY_LINK_ELEMENT.CODE_FRAGMENT_LINK_ID"
					+ ")INNER JOIN CODE_FRAGMENT"
					+ " ON CODE_FRAGMENT_LINK.AFTER_ELEMENT_ID = CODE_FRAGMENT.CODE_FRAGMENT_ID"
					+ ")INNER JOIN CRD"
					+ " ON CODE_FRAGMENT.CRD_ID = CRD.CRD_ID"
					+ ")INNER JOIN FILE"
					+ " ON CODE_FRAGMENT.OWNER_FILE_ID = FILE.FILE_ID"
					+ ")INNER JOIN REPOSITORY"
					+ " ON CODE_FRAGMENT.OWNER_REPOSITORY_ID = REPOSITORY.REPOSITORY_ID"
					+ ")INNER JOIN REVISION"
					+ " ON CODE_FRAGMENT_LINK.AFTER_COMBINED_REVISION_ID = REVISION.REVISION_ID"
				+ ")"
				+ "where CODE_FRAGMENT_LINK.AFTER_COMBINED_REVISION_ID = " + (i+1)
				+" AND (CODE_FRAGMENT.END_LINE - CODE_FRAGMENT.START_LINE + 1) > 5";
		return sql_after_fix;
	}

	public String link(int i) {
		String sql_link = "select CODE_FRAGMENT_LINK.BEFORE_ELEMENT_ID,"
				+ "CODE_FRAGMENT_LINK.AFTER_ELEMENT_ID "
				+ "from CODE_FRAGMENT_LINK "
				+ "where CODE_FRAGMENT_LINK.BEFORE_COMBINED_REVISION_ID = " + i;
		return sql_link;
	}

	public String binding(int i) {
		String sql_binding = "select CANDIDATE.*,REPOSITORY.REPOSITORY_ROOT_URL,FILE.FILE_PATH,REVISION.REVISION_IDENTIFIER,"
				+ "CODE_FRAGMENT.START_LINE,CODE_FRAGMENT.END_LINE "
				+ "from (((( "
				+ "CANDIDATE INNER JOIN CODE_FRAGMENT "
				+ "ON CANDIDATE.CODE_FRAGMENT_ID = CODE_FRAGMENT.CODE_FRAGMENT_ID "
				+ ")INNER JOIN FILE "
				+ "ON CODE_FRAGMENT.OWNER_FILE_ID = FILE.FILE_ID "
				+ ")INNER JOIN REPOSITORY "
				+ "ON CODE_FRAGMENT.OWNER_REPOSITORY_ID = REPOSITORY.REPOSITORY_ID "
				+ ")INNER JOIN REVISION "
				+ "ON CANDIDATE.REVISION = REVISION.REVISION_ID "
				+ ")where (CANDIDATE.REVISION = "+ (i+1) + " AND CANDIDATE.PROCESS = 'after_fix' ) OR (CANDIDATE.REVISION = " + (i+1) + " AND CANDIDATE.PROCESS = 'add') "
				+ "OR (CANDIDATE.REVISION = " + i + " AND CANDIDATE.PROCESS = 'before_fix') OR (CANDIDATE.REVISION = " + i + " AND CANDIDATE.PROCESS = 'delete' ) ";
		return sql_binding;
	}
}
