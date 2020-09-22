package ion.auth.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import ion.auth.dao.PasswordHasher;

public class BCryptHasher implements PasswordHasher {
	
	PasswordEncoder encoder = new BCryptPasswordEncoder();

	@Override
	public String hash(String password) {
		return encoder.encode(password);
	}

}
