package org.ballerinalang.openapi.validator;

public class ValidationError {
    String fieldName;
    Enum type;

    public void ValidationError(){
        fieldName = null;
        type = null;
    }
    public void ValidationError(String fieldName, Enum type){
        this.fieldName =fieldName;
        this.type = type;
    }

    public void setFieldName(String fieldName){
        this.fieldName = fieldName;
    }
    public void setType(Enum type){
        this.type = type;
    }
    public String getFieldName(){
        return fieldName;
    }
    public  Enum getType(){
        return type;
    }
}
