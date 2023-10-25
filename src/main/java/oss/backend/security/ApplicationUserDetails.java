package oss.backend.security;

import oss.backend.domain.Profile;
import oss.backend.repository.core.ProfileRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

@Repository
public class ApplicationUserDetails implements UserDetailsService {
    private final ProfileRepository profileRepository;

    public ApplicationUserDetails(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Profile profile = profileRepository.getProfileByUsername(username);
        if (profile == null) {
            throw new UsernameNotFoundException("User not found.");
        }
        return User.of(profile);
    }
}
