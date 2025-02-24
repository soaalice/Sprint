package dev.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import dev.CustomSession;
import dev.exceptions.VerbNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import mg.annotation.ErrorUrl;
import mg.annotation.Param;
import mg.annotation.RestApi;
import mg.annotation.uploads.FileBytes;
import mg.annotation.uploads.FileName;
import mg.annotation.validations.annotation.Validation;
import mg.annotation.validations.validateur.Validator;

import java.lang.annotation.Annotation;

public class Mapping {
    public String classe;
    private HashMap<Verb, Method> verbMethod = new HashMap<>();

    public boolean isRestApi(Verb verb){
        Method methode = verbMethod.get(verb);
        return methode.isAnnotationPresent(RestApi.class);
    }

    public String getErrorUrl(Verb verb){
        Method methode = verbMethod.get(verb);
        if (methode.isAnnotationPresent(ErrorUrl.class)) {
            return methode.getAnnotation(ErrorUrl.class).value();   
        }
        return null;
    }

    public Object invoke(HttpServletRequest request,Object obj, CustomSession session, Verb v, List<Exception> exceptions) throws Exception{

        Method methode = verbMethod.get(v);
        if (methode == null) {
            throw new VerbNotFoundException(v+" n'est pas assignee a cet url.");
        }

        Parameter[] parameterFunction = methode.getParameters();
        Object[] parameterValues=new Object[parameterFunction.length];
        for(int i=0;i<parameterFunction.length;i++){
            //Tetezina daholy izay nom de parametres nangatahina tany amin'ilay fonction an'ilay controller
            if(parameterFunction[i].isAnnotationPresent(Param.class)){
                Param paramName=parameterFunction[i].getAnnotation(Param.class);
                String parameterNameFunction=paramName.name();

                //Atao cles ilay nom de parametres de raha tsy misy izy de null no azo
                if(parameterFunction[i].getType().isPrimitive() || parameterFunction[i].getType().equals(String.class)){
                    parameterValues[i]=request.getParameter(parameterNameFunction);
                    validate(parameterValues[i], parameterFunction[i], exceptions);
                } else if (parameterFunction[i].getType().equals(CustomSession.class)) {
                    parameterValues[i] = session;
                } else {
                    HashMap<String,String> values=getAttributeValue(request,parameterNameFunction);
                    Object objValue=parameterFunction[i].getType().getConstructor().newInstance();
                    setValue(objValue, values, exceptions);
                    parameterValues[i]=objValue;
                }
            } 

            else if (parameterFunction[i].getType().equals(CustomSession.class)) {
                parameterValues[i] = session;
            } 

            else if (parameterFunction[i].isAnnotationPresent(FileName.class)) {
                FileName fileName = parameterFunction[i].getAnnotation(FileName.class);
                Part part = request.getPart(fileName.value());
                parameterValues[i] = part.getSubmittedFileName();
            } 

            else if (parameterFunction[i].isAnnotationPresent(FileBytes.class)) {
                FileBytes fileBytes = parameterFunction[i].getAnnotation(FileBytes.class);
                Part part = request.getPart(fileBytes.value());
                parameterValues[i] = part.getInputStream().readAllBytes();
            }

            else {
                throw new Exception("Ce parametre n'est pas annoté.");
            }
        }
        return methode.invoke(obj, parameterValues);
    }

    public HashMap<String,String> getAttributeValue(HttpServletRequest request,String parameterName){
        HashMap<String,String> valiny=new HashMap<String,String>();
        Enumeration<String> keys=request.getParameterNames();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            if(key.contains(parameterName+".")){
                valiny.put(key.split("[.]")[1], request.getParameter(key));
            }
        }
        return valiny;
    }

    public void validate(Object objParam,Parameter parameter,List<Exception> exceptions)throws Exception{
        Annotation[] annotations=parameter.getAnnotations();
        for (Annotation annotation : annotations) {
            if(annotation.annotationType().isAnnotationPresent(Validation.class)){
                Validation validation=annotation.annotationType().getAnnotation(Validation.class);
                Class<? extends Validator<?>> classe=validation.value();
                Validator<? extends Annotation> validator=classe.getConstructor().newInstance();
                Annotation annotationField=parameter.getAnnotation(validator.getAnnotation());
                Exception exception=validator.validateInside(objParam, annotationField);
                if(exception!=null){
                    exceptions.add(exception);
                }
            }
        }
    }

    public void validate(Object obj,Field field,List<Exception> exceptions)throws Exception{
        Annotation[] annotations=field.getAnnotations();
        for (Annotation annotation : annotations) {
            if(annotation.annotationType().isAnnotationPresent(Validation.class)){
                Validation validation=annotation.annotationType().getAnnotation(Validation.class);
                Class<? extends Validator<?>> classe=validation.value();
                Validator<? extends Annotation> validator=classe.getConstructor().newInstance();
                Annotation annotationField=field.getAnnotation(validator.getAnnotation());
                Exception exception=validator.validateInside(obj, annotationField);
                if(exception!=null){
                    exceptions.add(exception);
                }
            }
        }
    }

    public void validate(Object object,List<Exception> exceptions)throws Exception{
        Field[] arguments=object.getClass().getDeclaredFields();
        for (Field argument: arguments) {
            argument.setAccessible(true);
            validate(argument.get(object),argument, exceptions);
        }
    }

    public void setValue(Object obj,HashMap<String,String> values, List<Exception> exceptions)throws Exception{
        Set<String> keys=values.keySet();
        for (String key : keys) {
            String setterName="set"+key.substring(0,1).toUpperCase()+key.substring(1);
            Method[] methods=obj.getClass().getMethods();
            Method setter=null;
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
        validate(obj, exceptions);
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
