package org.apache.flink.lakesoul.test.ArrowTest;

import org.apache.arrow.vector.FieldVector;
import org.apache.arrow.vector.UInt4Vector;
import org.apache.arrow.vector.VarCharVector;
import org.apache.arrow.vector.VectorSchemaRoot;

import java.util.HashMap;
import java.util.List;

public class ArrowReadTest {
    public static void main(String[] args) {
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("_id","int");
        hashMap.put("age",null);
        System.out.println(hashMap);
        hashMap.put("age",12);
        hashMap.remove("name");
        System.out.println(hashMap);
    }

    private void vectorizePerson(int index, Person person, VectorSchemaRoot schemaRoot){
        ((VarCharVector) schemaRoot.getVector("firstName")).setSafe(index, person.getFirstName().getBytes());
        ((VarCharVector) schemaRoot.getVector("lastName")).setSafe(index, person.getLastName().getBytes());
        ((UInt4Vector) schemaRoot.getVector("age")).setSafe(index, person.getAge());
        List<FieldVector> childrenFromFields = schemaRoot.getVector("address").getChildrenFromFields();
        Addresss address = person.getAddress();
        ((VarCharVector) childrenFromFields.get(0)).setSafe(index, address.getStreet().getBytes());
        ((UInt4Vector) childrenFromFields.get(1)).setSafe(index, address.getStreetNumber());
        ((VarCharVector) childrenFromFields.get(2)).setSafe(index, address.getCity().getBytes());
        ((UInt4Vector) childrenFromFields.get(3)).setSafe(index, address.getPostalCode());
    }
    private void writeToArrowFile(Person[] people){
    }

}

class Person{
    String firstName;
    String lastName;
    int age;
    Addresss address;
    String street;
    public  Person(String firstName, String lastName, int age, Addresss address){
        this.firstName = firstName;
        this.lastName = lastName;
        this.age = age;
        this.address = address;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public Addresss getAddress() {
        return address;
    }

    public void setAddress(Addresss address) {
        this.address = address;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }
}
class Addresss{
    String street;
    int streetNumber;
    String city;
    int postalCode;
    public Addresss(String street, int streetNumber, String city, int postalCode){
        this.street = street;
        this.streetNumber = streetNumber;
        this.city = city;
        this.postalCode = postalCode;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public int getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(int streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(int postalCode) {
        this.postalCode = postalCode;
    }
}

