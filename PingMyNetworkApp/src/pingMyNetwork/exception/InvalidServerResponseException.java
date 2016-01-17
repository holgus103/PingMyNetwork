/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pingMyNetwork.exception;

/**
 * Exception indicating an invalid server response
 * @author Jakub Suchan
 * @version     %I%, %G%
 * @since       1.0
 */
public class InvalidServerResponseException extends Exception{

    /**
     * Constructor
     * @param message Exception message
     */
    public InvalidServerResponseException(String message) {
        super(message);
    }
    
}
