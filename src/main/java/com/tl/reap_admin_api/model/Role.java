package com.tl.reap_admin_api.model;

public enum Role {
    SUPER_ADMIN(1),
    NAR_ADMIN(2),
    NAR_STAFF(3),
    STATE_ADMIN(4),
    STATE_STAFF(5),
    RSETI_ADMIN(6),
    RSETI_STAFF(7),
    TRAINER(8),
    TRAINEE(9);

    private final int number;

    Role(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }

    public static Role fromNumber(int number) {
        for (Role role : Role.values()) {
            if (role.getNumber() == number) {
                return role;
            }
        }
        throw new IllegalArgumentException("Invalid role number: " + number);
    }
}
