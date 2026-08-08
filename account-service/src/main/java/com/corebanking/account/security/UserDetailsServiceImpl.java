package com.corebanking.account.security;

import com.corebanking.account.model.Customer;
import com.corebanking.account.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String identityNumber) throws UsernameNotFoundException {
        Customer customer = customerRepository.findByIdentityNumber(identityNumber)
                .orElseThrow(() -> new UsernameNotFoundException("User Not Found with identity: " + identityNumber));

        return UserDetailsImpl.build(customer);
    }
}
