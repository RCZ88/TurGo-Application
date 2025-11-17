package com.example.turgo;

import static com.example.turgo.Tool.hasParent;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public interface FirebaseClass<F>{
    /// 1. Create an empty firebaseObject (ex. StudentFirebase sf = new StudentFirebase())
    /// 2. Then use the method to import all the normalObject to the firebaseObject (sf.importObjectDataToFirebase(this))
    public abstract void importObjectData(F from);
    default <O extends RequireUpdate<?, ?>>ArrayList<String> convertToIdList(ArrayList<O> objectList){
        ArrayList<String> idList = new ArrayList<>();
        for (O obj : objectList) {
            idList.add(obj.getID());
        }
        return idList;
    }
    public abstract String getID();
    public F convertToNormal()throws ParseException, InvocationTargetException, NoSuchMethodException, IllegalAccessException, InstantiationException ;

    //class -> ? implements requireupdate
    default Object constructClass(Class<?>clazz, String id) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference(((RequireUpdate<?, ?>)clazz.getDeclaredConstructor().newInstance()).getFirebaseNode().getPath() + "/" + id);
        final AtomicReference<Object> object = new AtomicReference<>();
        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                try {
                    object.set(clazz.getDeclaredConstructor().newInstance());
                } catch (IllegalAccessException | InstantiationException |
                         InvocationTargetException | NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                ArrayList<Field> fields = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
                if(hasParent(clazz)){
                    fields.addAll(Arrays.asList(clazz.getSuperclass().getDeclaredFields()));
                }
                for(Field field : fields){
                    field.setAccessible(true);
                    if(field.getType().isPrimitive()){
                        Object value = snapshot.child(field.getName()).getValue(field.getType());
                        try {
                            field.set(object.get(), value);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }else if(field.getType() == ArrayList.class){
                        Type listType = ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                        Class<?> elementClass = null;
                        ArrayList<Object> list = new ArrayList<>();
                        if (listType instanceof Class<?>) {
                            elementClass = (Class<?>) listType;
                        }
                        for(Object listContent : snapshot.child(field.getName()).getChildren()){
                            assert elementClass != null;
                            String objectID = ((RequireUpdate<?, ?>)listContent).getID();
                            try {
                                list.add(constructClass(elementClass, objectID));
                            } catch (NoSuchMethodException | InvocationTargetException |
                                     IllegalAccessException | InstantiationException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        try {
                            field.set(object, list);
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }else if(field.getType() == LocalDateTime.class){
                        String fbLDT = snapshot.child(field.getName()).getValue(String.class);
                        try {
                            field.set(object.get(), LocalDateTime.parse(fbLDT));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }else if(field.getType() == LocalDate.class){
                        String fbLD = snapshot.child(field.getName()).getValue(String.class);
                        try {
                            field.set(object.get(), LocalDate.parse(fbLD));
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return object;

    }

    default ArrayList<?> getAllObject(FirebaseNode fbn){
        DatabaseReference dbf = FirebaseDatabase.getInstance().getReference(fbn.getPath());
        ArrayList<Object> objects = new ArrayList<>();
        dbf.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()){
                    objects.add(ds.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return objects;
    }
}
