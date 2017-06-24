package the.miner.engine.database;

import java.io.Serializable;
import java.util.List;

/**
 * Define a query string which include select, where .. statement and arguments
 */
public class GMQuery implements Serializable {

    private String[] mColumn;

    private String mWhere;
    private String[] mWhereArgs;

    private String mGroupBy;
    private String mHaving;
    private String mOrderBy;
    private String mLimit;

    private Object mTag;

    /**
     * Constructor
     *
     * @param column    selected columns
     * @param where     where statement. Use "?" for parameters.
     * @param whereArgs where arguments
     */
    public GMQuery(String[] column, String where, String[] whereArgs) {
        setColumn(column);
        setWhere(where);
        setWhereArgs(whereArgs);
    }

    /**
     * Constructor
     *
     * @param column    selected columns
     * @param where     where statement. Use "?" for parameters.
     * @param whereArgs where arguments
     */
    public GMQuery(String[] column, String where, List<String> whereArgs) {
        setColumn(column);
        setWhere(where);
        setWhereArgs(whereArgs.toArray(new String[whereArgs.size()]));
    }

    /**
     * Constructor
     *
     * @param column    selected columns
     * @param where     where statement. Use "?" for parameters.
     * @param whereArgs where arguments
     * @param orderBy   order condition. For example: "userName DESC"
     * @param limit     limit number. For example: "5"
     */
    public GMQuery(String[] column, String where, String[] whereArgs, String orderBy, String limit) {
        this(column, where, whereArgs);
        setOrderBy(orderBy);
        setLimit(limit);
    }

    /**
     * Constructor
     *
     * @param column    selected columns
     * @param where     where statement. Use "?" for parameters.
     * @param whereArgs where arguments
     * @param groupBy   group by condition. For example: "gender"
     * @param having    having condition. For example: "length(category) > 10"
     * @param orderBy   order condition. For example: "userName DESC"
     * @param limit     limit number. For example: "5"
     */
    public GMQuery(String[] column, String where, String[] whereArgs,
                   String groupBy, String having,
                   String orderBy, String limit) {
        this(column, where, whereArgs, orderBy, limit);
        setGroupBy(groupBy);
        setHaving(having);
    }


    /* --------------------- GET-SET ------------------------- */

    /**
     * Get selected columns
     *
     * @return selected columns
     */
    public String[] getColumn() {
        return mColumn;
    }

    /**
     * Set selected columns
     *
     * @param column selected column
     */
    public void setColumn(String[] column) {
        this.mColumn = column;
    }

    /**
     * Get where statement
     *
     * @return where statement
     */
    public String getWhere() {
        return mWhere;
    }

    /**
     * Set where statement. Use "?" for parameters.
     * For example: "gender = ? and userName like ?"
     *
     * @param where where statement
     */
    public void setWhere(String where) {
        this.mWhere = where;
    }

    /**
     * Get where arguments
     *
     * @return arguments for where statement
     */
    public String[] getWhereArgs() {
        return mWhereArgs;
    }

    /**
     * Set arguments for where
     *
     * @param whereArgs arguments
     */
    public void setWhereArgs(String[] whereArgs) {
        this.mWhereArgs = whereArgs;
    }

    /**
     * Get group by condition.
     *
     * @return group by.
     */
    public String getGroupBy() {
        return mGroupBy;
    }

    /**
     * Set group by condition. For example: "gender"
     * Do not include "GROUP BY" syntax
     *
     * @param groupBy group by condition
     */
    public void setGroupBy(String groupBy) {
        this.mGroupBy = groupBy;
    }

    /**
     * Get having condition
     *
     * @return having condition
     */
    public String getHaving() {
        return mHaving;
    }

    /**
     * Set having condition. For example: "length(category) > 10"
     * Do not include "HAVING" syntax
     *
     * @param having having condition
     */
    public void setHaving(String having) {
        this.mHaving = having;
    }

    /**
     * Get order condition.
     *
     * @return order condition
     */
    public String getOrderBy() {
        return mOrderBy;
    }

    /**
     * Set order condition. For example: "userName DESC"
     * Do not inclide "ORDER" syntax
     *
     * @param orderBy
     */
    public void setOrderBy(String orderBy) {
        this.mOrderBy = orderBy;

    }

    /**
     * Limits the number of rows.
     *
     * @return limit number
     */
    public String getLimit() {
        return mLimit;
    }

    /**
     * Set limit number. For example: "5"
     * Do not include "LIMIT" syntax
     *
     * @param mLimit
     */
    public void setLimit(String mLimit) {
        this.mLimit = mLimit;
    }

    /**
     * Get tag
     *
     * @return tagged object
     */
    public Object getTag() {
        return mTag;
    }

    /**
     * Set tag object
     *
     * @param tag tagged object
     */
    public void setTag(Object tag) {
        mTag = tag;
    }

    /* ---------------------- METHOD ------------------------- */

}
