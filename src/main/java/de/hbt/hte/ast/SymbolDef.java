package de.hbt.hte.ast;

import de.hbt.hte.parser.*;
import lombok.*;

public @Data class SymbolDef implements SourceLocatable {

    public int startLine;
    public int endLine;
    public int startColumn;
    public int endColumn;
    public final Token name;
    public final Expression expression;

    public void location(int startLine, int startColumn, int endLine, int endColumn) {
        if (startLine > endLine) {
            throw new IllegalArgumentException(
                    "startLine " + startLine + " is not less than or equal to endLine " + endLine);
        }
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name.image);
        if (expression != null) {
            sb.append(" = ");
            sb.append(expression);
        }
        return sb.toString();
    }

    public String toApiString() {
        return null;
    }

}
