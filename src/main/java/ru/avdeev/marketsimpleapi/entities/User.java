package ru.avdeev.marketsimpleapi.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Data
@Table(name="usr")
public class User implements UserDetails {

    @Id
    @Column("id")
    private UUID id;

    @Column("username")
    private String username;

    @Column("password")
    @JsonIgnore
    private String password;

    @Transient
    private List<Role> roles;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        List<GrantedAuthority> grantedAuthorities = new ArrayList<>();
        for(Role userRole : roles) grantedAuthorities.add(new SimpleGrantedAuthority(userRole.getRole().name()));
        return grantedAuthorities;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public enum UserRole {
        ROLE_USER,
        ROLE_ADMIN
    }
}
