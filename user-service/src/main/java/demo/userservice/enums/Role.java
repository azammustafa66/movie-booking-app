package demo.userservice.enums;

/** Authorization role of an {@code AppUser}. New signups default to {@link #CUSTOMER}. */
public enum Role {
    CUSTOMER,
    ADMIN,
    VENDOR
}
