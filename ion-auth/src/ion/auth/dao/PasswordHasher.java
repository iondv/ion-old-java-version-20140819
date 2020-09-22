package ion.auth.dao;

public interface PasswordHasher {
	public String hash(String password);
}
