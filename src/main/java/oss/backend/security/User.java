package oss.backend.security;

import oss.backend.domain.Profile;
import oss.backend.util.MappingUtils;

import java.io.Serial;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class User implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1L;

    private final Profile profile;
    private final GrantedAuthority userRole;

    private User(Profile profile) {
        this.profile = Objects.requireNonNull(profile, "profile can't be null.");
        this.userRole = new SimpleGrantedAuthority("USER_ROLE");
    }

    public long getProfileId() {
        return profile.id();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(userRole);
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return new BCryptPasswordEncoder().encode(profile.password());
    }

    public String getUsername() {
        return profile.username();
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    @JsonIgnore
    public String getFirstName() {
        return profile.firstName();
    }

    @JsonIgnore
    public String getLastName() {
        return profile.lastName();
    }

    @JsonIgnore
    public Profile getProfile() {
        return profile;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        User user = (User) o;
        return getProfileId() == user.getProfileId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getProfileId());
    }

    @Override
    public String toString() {
        return MappingUtils.convertObjectToJson(this);
    }

    public static User of(Profile profile) {
        return new User(profile);
    }
}
