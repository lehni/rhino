package org.mozilla.javascript;

public interface OperatorHandler {
    public Object handleOperator(Context cx, Scriptable scope, int operator, Object lhs, Object rhs);
    public Object handleSignOperator(Context cx, Scriptable scope, int operator, Object rhs);
}
