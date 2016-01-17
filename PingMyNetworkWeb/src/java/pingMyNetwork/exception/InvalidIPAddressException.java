/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.exception;

/**
 * Exception indicating an invalid IP address
 * @author Jakub Suchan
 * @version     %I%, %G%
 * @since       1.0
 */
public class InvalidIPAddressException extends Exception{
    
    /**
     * Constructor of my own custom exception
     * @param message Message to initialize the exception with
     */
    public InvalidIPAddressException(String message) {
        super(message);
    }
    
    
}
