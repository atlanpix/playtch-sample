package models.entities;

import javax.validation.*;

import play.data.validation.Constraints.*;

public class User {
   
    public interface All {}

    @Required(groups = {All.class})
    @MinLength(value = 4, groups = {All.class})
    public String username;

    @Email(groups = {All.class})
    public String email;
    
    @Required(groups = {All.class})
    @MinLength(value = 6, groups = {All.class})
    public String password;

    @Valid
    public Profile profile;

    public String latchAccountId;
    
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(String username, String password, String latchAccountId) {
        this.username = username;
        this.password = password;
        this.latchAccountId = latchAccountId;
    }

    public User(String username, String email, String password, Profile profile) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = profile;
    }

    public User(String username, String email, String password, Profile profile, String latchAccountId) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.profile = profile;
        this.latchAccountId = latchAccountId;
    }
    
    public static class Profile {
        
        @Required(groups = {All.class})
        public String country;
        
        public String address;
        
        @Min(value = 18, groups = {All.class}) @Max(value = 100, groups = {All.class})
        public Integer age;
        
        public Profile() {}
        
        public Profile(String country, String address, Integer age) {
            this.country = country;
            this.address = address;
            this.age = age;
        }
        
    }
    
}