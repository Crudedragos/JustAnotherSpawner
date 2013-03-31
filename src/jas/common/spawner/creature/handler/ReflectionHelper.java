package jas.common.spawner.creature.handler;

import jas.common.JASLog;

import java.lang.reflect.Field;

public class ReflectionHelper {
    /**
     * Helper used to See if we are unObfuscated by checking for a known non-Obfuscated name
     * return true if unObfuscated (eclipse), false if obfuscated (Minecraft)
     * @param list
     */
    public static boolean isUnObfuscated(Class<?> regularClass, String regularClassName){
        return regularClass.getSimpleName().equals(regularClassName);
    }
    
    /**
     * Helper method to Perform Reflection to Set non-static Field of Provided Type. Field is assumed Private.
     * 
     * @param fieldName
     * @param containingClass Class that contains desired field containerInstance should be castable to it. Required to
     *            get fields from parent classes
     * @param containterInstance Instance of the Object to get the non-static Field
     * @param isPrivate Whether the field is private and requires setAccessible(true)
     * @param type
     * @param value
     * @return
     */
    public static <T> void setFieldUsingReflection(String fieldName, Class<?> containingClass, Object containterInstance, boolean isPrivate, T value){
        try {
            Field desiredField = containingClass.getDeclaredField(fieldName);
            if (isPrivate) {
                desiredField.setAccessible(true);
            }
            desiredField.set(containterInstance, value);
        }catch (NoSuchFieldException e) {
            JASLog.severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.", fieldName);
            e.printStackTrace();
        }catch (IllegalArgumentException e) {
            JASLog.severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.", fieldName);
            e.printStackTrace();
        }catch (IllegalAccessException e) {
            JASLog.severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.", fieldName);
            e.printStackTrace();
        }catch (SecurityException e) {
            JASLog.severe("Obfuscation needs to be updated to access the %s. Please notify modmaker Immediately.", fieldName);
            e.printStackTrace();
        }
    }
}