package org.ballerinalang.openapi.validator;

public class ValidationError {
    String fieldName;
    String type;

    public void ValidationError(){
        fieldName = null;
        type = null;
    }
    public void ValidationError(String fieldName, String type){
        this.fieldName =fieldName;
        this.type = type;
    }

    public void setFieldName(String fieldName){
        this.fieldName = fieldName;
    }
    public void setType(String type){
        this.type = type;
    }
    public String getFieldName(){
        return fieldName;
    }
    public  String getType(){
        return type;
    }
}
