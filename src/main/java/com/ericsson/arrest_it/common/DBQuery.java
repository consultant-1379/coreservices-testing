package com.ericsson.arrest_it.common;

public class DBQuery {
    private String statement;
    private String templateStatement;
    private boolean isRepeating;
    private String instruction;
    private boolean isInstructionPresent;

    public DBQuery(final String templateStatement, final boolean isRepeating, final String instruction) {
        this.templateStatement = templateStatement;
        this.isRepeating = isRepeating;
        this.setInstruction(instruction);
        this.isInstructionPresent = true;
    }

    public DBQuery(final String templateStatement, final boolean isRepeating) {
        this.templateStatement = templateStatement;
        this.isRepeating = isRepeating;
        this.isInstructionPresent = false;
    }

    public DBQuery(final String statement) {
        this.statement = statement;
        this.isRepeating = true;
    }

    public DBQuery(final DBQuery another) {
        this.templateStatement = new String(another.getTemplateStatement());
        this.isRepeating = another.isRepeating();
        this.instruction = another.getInstruction();
        this.isInstructionPresent = another.isInstructionPresent();
    }

    public String getStatement() {
        return statement;
    }

    public void setStatement(final String statement) {
        this.statement = statement;
    }

    public boolean isRepeating() {
        return isRepeating;
    }

    public void setRepeating(final boolean isRepeating) {
        this.isRepeating = isRepeating;
    }

    public boolean isInstructionPresent() {
        return isInstructionPresent;
    }

    public void setInstructionPresent(final boolean isInstructionPresent) {
        this.isInstructionPresent = isInstructionPresent;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(final String instruction) {
        this.instruction = instruction;
    }

    public String getTemplateStatement() {
        return templateStatement;
    }

    public void setTemplateStatement(final String templateStatement) {
        this.templateStatement = templateStatement;
    }

}
