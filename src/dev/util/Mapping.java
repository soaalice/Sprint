package dev.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Set;

import dev.CustomSession;
import jakarta.servlet.http.HttpSession;
import mg.annotation.Param;
import mg.annotation.RestApi;

public class Mapping {
    public String classe;
    // public Method methode;

    private HashMap<Verb, Method> verbMethod;

    public boolean isRestApi(){
        return methode.isAnnotationPresent(RestApi.class);
    }

    public Object invoke(HashMap<String,String> requestParameter,Object obj, CustomSession session, Verb v) throws Exception{
        Method methode = verbMethod.get(v);
        Parameter[] parameterFunction = methode.getParameters();
        Object[] parameterValues=new Object[parameterFunction.length];
        for(int i=0;i<parameterFunction.length;i++){
            //Tetezina daholy izay nom de parametres nangatahina tany amin'ilay fonction an'ilay controller
            if(parameterFunction[i].isAnnotationPresent(Param.class)){
                Param paramName=parameterFunction[i].getAnnotation(Param.class);
                String parameterNameFunction=paramName.name();
                //Atao cles ilay nom de parametres de raha tsy misy izy de null no azo
                System.out.println(parameterNameFunction);
                if(parameterFunction[i].getType().isPrimitive() || parameterFunction[i].getType().equals(String.class)){
                    parameterValues[i]=requestParameter.get(parameterNameFunction);
                }
                else if (parameterFunction[i].getType().equals(CustomSession.class)) {
                    parameterValues[i] = session;
                } 
                else {
                    HashMap<String,String> values=getAttributeValue(requestParameter,parameterNameFunction);
                    Object objValue=parameterFunction[i].getType().getConstructor().newInstance();
                    setValue(objValue, values);
                    parameterValues[i]=objValue;
                }
                System.out.println(parameterValues[i]);
            } 
            else if (parameterFunction[i].getType().equals(CustomSession.class)) {
                parameterValues[i] = session;
            }
            else {
                throw new Exception("Ce parametre n'est pas annoté.");
            }
        }
        return methode.invoke(obj, parameterValues);
    }

    public HashMap<String,String> getAttributeValue(HashMap<String,String> requestParameter,String parameterName){
        HashMap<String,String> valiny=new HashMap<String,String>();
        Set<String> keys=requestParameter.keySet();
        for (String key : keys) {
            if(key.contains(parameterName+".")){
                valiny.put(key.split("[.]")[1], requestParameter.get(key));
            }
        }
        return valiny;
    }

    public void setValue(Object obj,HashMap<String,String> values)throws Exception{
        Set<String> keys=values.keySet();
        for (String key : keys) {
            String setterName="set"+key.substring(0,1).toUpperCase()+key.substring(1);
            Method[] methods=obj.getClass().getMethods();
            Method setter=null;
            System.out.println(methods.length);
            for(int i=0;i<methods.length;i++){
                System.out.println(setterName+" "+ methods[i].getName());
                if(methods[i].getName().compareTo(setterName)==0 && methods[i].getParameterTypes().length==1){
                    setter=methods[i];
                    break;
                }
            }
            Object value=parse(values.get(key),setter.getParameterTypes()[0]);
            setter.invoke(obj, value);
        }
    }

    public Object parse(String value,Class<?> type){
        if(type == int.class){
            return Integer.parseInt(value);
        }
        if(type == double.class){
            return Double.parseDouble(value);
        }
        if (type == float.class) {
            return Float.parseFloat(value);
        }
        return value;
    }

    public void setVerbMethod(Verb verb, Method method) throws Exception{
        if (verbMethod.containsKey(verb)) {
            throw new Exception("La methode "+method.getName()+" est deja annotee a "+verb.toString());
        }
        verbMethod.put(verb, method);
    }
}
