package dev.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;

import mg.annotation.Param;

public class Mapping {
    public String classe;
    public Method methode;

    public Object invoke(HashMap<String,String> requestParameter,Object obj) throws Exception{
        Parameter[] parameterFunction=this.methode.getParameters();
        Object[] parameterValues=new Object[parameterFunction.length];
        for(int i=0;i<parameterFunction.length;i++){
            //Tetezina daholy izay nom de parametres nangatahina tany amin'ilay fonction an'ilay controller
            if(parameterFunction[i].isAnnotationPresent(Param.class)){
                Param paramName=parameterFunction[i].getAnnotation(Param.class);
                String parameterNameFunction=paramName.name();
                //Atao cles ilay nom de parametres de raha tsy misy izy de null no azo
                parameterValues[i]=requestParameter.get(parameterNameFunction);
            }
        }
        return this.methode.invoke(obj, parameterValues);
    }
}
