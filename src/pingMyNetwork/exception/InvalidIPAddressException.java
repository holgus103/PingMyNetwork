/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.exception;

/**
 *
 * @author holgus103
 * @version %I%
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
