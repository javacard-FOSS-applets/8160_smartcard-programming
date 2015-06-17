package Identification;

import javacard.framework.*;

/**
 * Created by Patrick on 16.06.2015.
 */
public class Identification extends Applet {
    // Java Card
    // Applet
    private static final byte IDENTIFICATION_CLA = 0x49;

    // Instructions
    private static final byte SET_NAME = (byte) 0xA0;
    private static final byte GET_NAME = (byte) 0xA1;

    private static final byte SET_BIRTHDAY = (byte) 0xB0;
    private static final byte CHECK_AGE = (byte) 0xB1;

    private static final byte SET_CARID = (byte) 0xC0;
    private static final byte GET_CARID = (byte) 0xC1;

    private static final byte SET_SAFEPIN = (byte) 0xD0;
    private static final byte CHECK_SAFEPIN = (byte) 0xD1;

    // Data
    private short MAX_NAME_LENGTH = 50;
    private byte[] name;

    private short MAX_BIRTHDAY_LENGTH = 10;
    private byte[] birthDay;

    private short MAX_CARID_LENGTH = 8;
    private byte[] carId;

    private short MAX_SAFEPIN_LENGTH = 4;
    private byte[] safePin;

    protected Identification() {
        register();

        name = new byte[MAX_NAME_LENGTH];
        birthDay = new byte[MAX_BIRTHDAY_LENGTH];
        carId = new byte[MAX_CARID_LENGTH];
        safePin = new byte[MAX_SAFEPIN_LENGTH];
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new Identification();
    }

    @Override
    public void process(APDU apdu) throws ISOException {
        byte[] buf = apdu.getBuffer();
        if (buf[ISO7816.OFFSET_CLA] == IDENTIFICATION_CLA) {
            switch (buf[ISO7816.OFFSET_INS]) {
                case SET_NAME:
                    setName(apdu);
                    break;
                case GET_NAME:
                    getName(apdu);
                    break;
                case SET_BIRTHDAY:
                    setBirthday(apdu);
                    break;
                case CHECK_AGE:
                    checkAge(apdu);
                    break;
                case SET_CARID:
                    setCarId(apdu);
                    break;
                case GET_CARID:
                    getCarId(apdu);
                    break;
                case SET_SAFEPIN:
                    setSafePin(apdu);
                    break;
                case CHECK_SAFEPIN:
                    checkSafePin(apdu);
                    break;
                case ISO7816.CLA_ISO7816:
                    if (selectingApplet()) {
                        ISOException.throwIt(ISO7816.SW_NO_ERROR);
                    }
                    break;
                default:
                    ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
            }
        } else {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
    }

    private void checkSafePin(APDU apdu) {

    }

    private void setSafePin(APDU apdu) {

    }

    private void getCarId(APDU apdu) {

    }

    private void setCarId(APDU apdu) {

    }

    private void checkAge(APDU apdu) {

    }

    private void setBirthday(APDU apdu) {

    }

    private void getName(APDU apdu) {

    }

    private void setName(APDU apdu) {

    }
}
