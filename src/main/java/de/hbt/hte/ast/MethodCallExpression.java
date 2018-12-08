package de.hbt.hte.ast;

import java.util.Iterator;
import java.util.List;

import lombok.*;

@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public @Data class MethodCallExpression extends Expression {

    public final Expression receiver;
    public final SymbolRef name;
    public final List<Expression> arguments;

    public void accept(ExpressionVisitor v) {
        v.visitMethodCallExpression(this);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(receiver);
        sb.append(".");
        sb.append(name);
        sb.append("(");
        if (arguments != null) {
            Iterator<? extends Expression> it = arguments.iterator();
            while (it.hasNext()) {
                Expression e = it.next();
                sb.append(e.toString());
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public String toApiString() {
        StringBuilder sb = new StringBuilder();
        sb.append("new ");
        sb.append(MethodCallExpression.class.getSimpleName());
        sb.append("(");
        sb.append(receiver.toApiString());
        sb.append(", ");
        sb.append("\"");
        sb.append(name);
        sb.append("\", Arrays.asList(");
        for (Expression arg : arguments) {
            sb.append(", ");
            sb.append(arg.toApiString());
        }
        sb.append("))");
        return sb.toString();
    }

}
