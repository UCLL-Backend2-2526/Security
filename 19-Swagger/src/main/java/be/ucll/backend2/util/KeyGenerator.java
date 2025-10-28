void main() {
    byte[] key = new byte[32];
    new SecureRandom().nextBytes(key);
    final var base64Key = Base64.getUrlEncoder().withoutPadding().encodeToString(key);
    System.out.println(base64Key);
}
