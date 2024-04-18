package io.yanmulin.codesnippets.patterns.builder;

import lombok.AllArgsConstructor;

import java.util.*;

/**
 * TODO:
 * - AND, OR
 * - Data type
 * - functions
 * - arbitrary combination of expressions
 * - HAVING clause
 * - sub-query
 * - CTE statement
 */
public class SelectSQLBuilder {

    public enum OrderEnum {
        ASC, DESC;
    }

    @AllArgsConstructor
    public enum RuleType {
        SELECT(0, "SELECT %s", 1),
        FROM(1, "FROM %s", 1),
        JOIN(2, "JOIN %s ON %s", 2),
        WHERE(3, "WHERE %s", 1),
        GROUP_BY(4, "GROUP BY %s", 1),
//        HAVING(5, "HAVING %s", 1),
        ORDER_BY(6, "ORDER BY %s", 2),
        LIMIT(7, "LIMIT %s OFFSET %s", 2),
        ;
        private int order;
        private String template;
        private int argsNum;
    }

    @AllArgsConstructor
    private class Rule implements Comparable {
        RuleType type;
        String[] args;

        @Override
        public int compareTo(Object o) {
            if (o instanceof Rule) {
                return type.compareTo(((Rule) o).type);
            }
            throw new UnsupportedOperationException("unable to compare " + o);
        }
    }

    private String fromTable;
    private List<Rule> rules = new ArrayList<>();

    private SelectSQLBuilder add(RuleType type, String... args) {
        if (args.length != type.argsNum) {
            throw new IllegalArgumentException(String.format("%s needs %d args, but received %d",
                    type.name(), type.argsNum, args.length));
        }
        rules.add(new Rule(type, args));
        return this;
    }

    public SelectSQLBuilder(String from) {
        fromTable = from;
        add(RuleType.FROM, from);
    }

    public SelectSQLBuilder select(String column) {
        return add(RuleType.SELECT, column);
    }

    public SelectSQLBuilder min(String column) {
        return add(RuleType.SELECT, "MIN(" + column + ")");
    }

    public SelectSQLBuilder max(String column) {
        return add(RuleType.SELECT, "MAX(" + column + ")");
    }

    public SelectSQLBuilder count(String column) {
        return add(RuleType.SELECT, "COUNT(" + column + ")");
    }

    public SelectSQLBuilder avg(String column) {
        return add(RuleType.SELECT, "AVG(" + column + ")");
    }

    public SelectSQLBuilder join(String table, String column) {
        return add(RuleType.JOIN, table, String.format("%s.%s=%s.%s", table, column, fromTable, column));
    }

    public SelectSQLBuilder eq(String column, String value) {
        return add(RuleType.WHERE, String.format("%s = '%s'", column, value));
    }

    public SelectSQLBuilder eq(String column, int value) {
        return add(RuleType.WHERE, String.format("%s = %s", column, value));
    }

    public SelectSQLBuilder lt(String column, int value) {
        return add(RuleType.WHERE, String.format("%s < %s", column, value));
    }

    public SelectSQLBuilder gt(String column, int value) {
        return add(RuleType.WHERE, String.format("%s > %s", column, value));
    }

    public SelectSQLBuilder like(String column, String value) {
        return add(RuleType.WHERE, String.format("%s LIKE '%%%s%%'", column, value));
    }

    public SelectSQLBuilder in(String column, int... values) {
        StringJoiner sj = new StringJoiner(",");
        for (int value: values) {
            sj.add(String.valueOf(value));
        }
        if (values.length == 1) {
            sj.add("");
        }
        return add(RuleType.WHERE, String.format("%s IN (%s)", column, sj));
    }

    public SelectSQLBuilder groupBy(String column) {
        return add(RuleType.GROUP_BY, column);
    }

    public SelectSQLBuilder orderBy(String column) {
        return orderBy(column, OrderEnum.DESC);
    }

    public SelectSQLBuilder orderBy(String column, OrderEnum order) {
        return add(RuleType.ORDER_BY, column, order.name());
    }

    public SelectSQLBuilder limit(int size, int offset) {
        return add(RuleType.LIMIT, String.valueOf(size), String.valueOf(offset));
    }

    public SelectSQLBuilder limit(int size) {
        return limit(size, 0);
    }

    private void mergeSort(Rule[] array, Rule[] aux, int start, int end) {
        if (end <= start) return;
        int mid = (end + start) / 2;
        mergeSort(array, aux, start, mid);
        mergeSort(array, aux, mid + 1, end);

        int l = start, r = mid + 1;
        for (int p=start;p<=end;p++) {
            if (r > end || (l <= mid && array[l].compareTo(array[r]) <= 0)) {
                aux[p] = array[l];
                l ++;
            } else {
                aux[p] = array[r];
                r ++;
            }
        }

        for (int i=start;i<=end;i++) {
            array[i] = aux[i];
        }


    }

    public String build() {
        Rule[] rules = this.rules.toArray(new Rule[0]);
        Rule[] aux = new Rule[rules.length];
        mergeSort(rules, aux, 0, rules.length - 1);  // stable sort

        int cur = 0;
        int length = rules.length;
        StringBuilder sql = new StringBuilder();

        // SELECT clause
        if (length == 0 || !rules[cur].type.equals(RuleType.SELECT)) {
            sql.append(String.format(RuleType.SELECT.template, "*"));
        } else {
            StringJoiner select = new StringJoiner(", ");
            while (cur < length && rules[cur].type.equals(RuleType.SELECT)) {
                select.add(rules[cur].args[0]);
                cur ++;
            }
            sql.append(String.format(RuleType.SELECT.template, select));
        }
        sql.append("\n");

        // FROM clause
        if (cur == length || !rules[cur].type.equals(RuleType.FROM)) {
            throw new IllegalStateException("no FROM clause");
        }
        sql.append(String.format(RuleType.FROM.template, rules[cur].args[0]));
        sql.append("\n");
        cur ++;

        // JOIN clause
        while (cur < length && rules[cur].type.equals(RuleType.JOIN)) {
            Rule rule = rules[cur];
            sql.append(String.format(rule.type.template, rule.args[0], rule.args[1]));
            sql.append("\n");
            cur ++;
        }

        // WHERE clause
        if (cur < length && rules[cur].type.equals(RuleType.WHERE)) {
            StringJoiner where = new StringJoiner(" AND ");
            while (cur < length && rules[cur].type.equals(RuleType.WHERE)) {
                where.add(rules[cur].args[0]);
                cur++;
            }
            sql.append(String.format(RuleType.WHERE.template, where));
            sql.append("\n");
        }

        // GROUP BY clause
        if (cur < length && rules[cur].type.equals(RuleType.GROUP_BY)) {
            StringJoiner groupBy = new StringJoiner(", ");
            while (cur < length && rules[cur].type.equals(RuleType.GROUP_BY)) {
                groupBy.add(rules[cur].args[0]);
                cur++;
            }
            sql.append(String.format(RuleType.GROUP_BY.template, groupBy));
            sql.append("\n");
        }

        // ORDER BY clause
        if (cur < length && rules[cur].type.equals(RuleType.ORDER_BY)) {
            StringJoiner orderBy = new StringJoiner(", ");
            while (cur < length && rules[cur].type.equals(RuleType.ORDER_BY)) {
                orderBy.add(rules[cur].args[0] + " " + rules[cur].args[1]);
                cur++;
            }
            sql.append(String.format(RuleType.ORDER_BY.template, orderBy));
            sql.append("\n");
        }

        // LIMIT clause
        if (cur < length && rules[cur].type.equals(RuleType.LIMIT)) {
            Rule rule = rules[cur];
            sql.append(String.format(RuleType.LIMIT.template, rule.args[0], rule.args[1]));
            sql.append("\n");
            cur ++;
        }

        if (cur != length) {
            throw new IllegalStateException("redundant rule " + rules[cur].type.name());
        }

        return sql.toString();
    }

    public static void main(String[] args) {
        System.out.println(new SelectSQLBuilder("test_tbl")
                .select("col1").min("col2").max("col3").avg("col4").count("col5")
                .join("join_tbl", "col1")
                .eq("col1", 5).eq("col2", "john").lt("col3", 999)
                .like("col4", "abc").in("col5", 1)
                .groupBy("col1").groupBy("col2")
                .orderBy("col3").orderBy("col4", OrderEnum.ASC)
                .limit(10, 100)
                .build());
    }
}
