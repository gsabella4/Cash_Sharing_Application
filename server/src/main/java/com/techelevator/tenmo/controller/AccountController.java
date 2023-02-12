package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.List;
import java.util.Map;

// Controller to implement account activities in db
@PreAuthorize("isAuthenticated()")
@RestController
public class AccountController {
    /**
     * The new request methods have yet to be tested!! But do Work
     */
    private AccountDao accountDao;
    private UserDao userDao;
    private TransferDao transferDao;
    private int currentUserId;

    public AccountController(AccountDao accountDao, UserDao userDao, TransferDao transferDao) {
        this.accountDao = accountDao;
        this.userDao = userDao;
        this.transferDao = transferDao;
    }

    //Account Creation PT2

    /**
     * One User has access to multiple accounts
     * @param currentUser
     *
     * Side Note: for each new account made by same user it automatically has $1000 not sure if fix needed here.
     *
     * Bugs/features?:
     * CANNOT transfer funds between accounts with same USER_ID
     * Multiple accounts made by same users are connected TO SAME FUNDS *** so the $1000^ from Sidenote is all the same money
     *
     */
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(value="/registerAccount", method=RequestMethod.POST)
    public void createAccount(Principal currentUser) {
        // Get the ID of the current user
        currentUserId = userDao.findByUsername(currentUser.getName()).getId();

        // Create an account for the current user
        accountDao.create(currentUserId);
    }

    @RequestMapping(path="/accounts/balance", method=RequestMethod.GET)
    public BigDecimal getBalance(Principal currentUser) {
        // Check if currentUser is null
        if (currentUser == null) {
            throw new IllegalArgumentException("currentUser cannot be null");
        }

        // Get the balance of the current user's account
        BigDecimal balance = accountDao.getFunds(currentUser.getName());

        // Check if the balance is null
        if (balance == null) {
            // If the balance is null, throw exception
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        // Check if the balance is zero
        if (balance.equals(BigDecimal.ZERO)) {
            // If the balance is zero, throw exception
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        // Return the balance
        return balance;
    }

    @RequestMapping(path="/users", method=RequestMethod.GET)
    public Map<Integer, String> getUserList(Principal currentUser) {
        // Get the ID of the current user
        currentUserId = userDao.findByUsername(currentUser.getName()).getId();

        // Get a map of all user IDs and names for the current user
        Map<Integer, String> userMap = userDao.findAllUserIdAndName(currentUserId);

        // Check if the map is empty
        if (userMap.isEmpty()) {
            // If the map is empty, throw exception
            throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "You're the only teapot here :)");
        }

        // Return the map
        return userMap;
    }

    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public Transfer instantTransfer(@Valid @RequestBody Transfer transfer, Principal currentUser) {
        // Check if transfer and currentUser are null
        if (transfer == null || currentUser == null) {
            throw new IllegalArgumentException("transfer and currentUser cannot be null");
        }

        // Get the ID of the current user
        currentUserId = userDao.findByUsername(currentUser.getName()).getId();

        // Get the sending and receiving accounts
        Account sendingAccount = accountDao.getAccountById(transfer.getSending_account_id());
        Account receivingAccount = accountDao.getAccountById(transfer.getReceiving_account_id());

        // Check if the current user has sufficient funds in their account
        if (accountDao.getFunds(currentUser.getName()).compareTo(transfer.getTransfer_amount()) != -1) {

            // Check if the current user is the owner of the sending account
            if (currentUserId == sendingAccount.getUserId()) {

                // Check if the sending and receiving accounts belong to different users
                if (sendingAccount.getUserId() != receivingAccount.getUserId()) {

                    // Get a map of all users with their ID and name
                    Map<Integer, String> allUsers = userDao.findAllUserIdAndName(currentUserId);

                    // Check if the user associated with the receiving account exists in the map
                    if (allUsers.containsKey(receivingAccount.getUserId())) {

                        // Transfer the funds
                        transferDao.transfer(sendingAccount, receivingAccount, transfer.getTransfer_amount());
                        return transfer;
                    }
                    // If the user associated with the receiving account doesn't exists in the map, throw exception
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Receiving user not found");
                }
                // If the sending and receiving accounts belong to same users, throw exception
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Cannot transfer funds to yourself");
            }
            // If the current user is not the owner of the sending account, throw exception
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Unauthorized to make this transfer");
        }
        // Check if the current user does not have sufficient funds in their account, throw exception
        throw new ResponseStatusException(HttpStatus.CONFLICT,"Insufficient funds in sending account");
    }

    //returns all transfer Objects related to current logged-in user
    @RequestMapping(path= "/myTransfers", method= RequestMethod.GET)
    public List<Transfer> getMyTransferList(Principal principal){
         // Get the current user's ID
         currentUserId = userDao.findByUsername(principal.getName()).getId();

         // Get the current user's transfers
         List<Transfer> transfers = transferDao.getAllTransfers(currentUserId);

         // Check if the user has any transfers
         if (transfers.size() > 0) {
             return transfers;
         } else {
             // The user does not have any transfers
             throw new ResponseStatusException(HttpStatus.I_AM_A_TEAPOT, "Do some transfers");
         }
     }

    //returns Transfer object w/ given transfer_id(Path Variable)
    @RequestMapping(path= "/transfer/{id}", method= RequestMethod.GET)
    public Transfer getTransferById(@PathVariable int id, Principal currentUser){
        // Get the current user's ID
        currentUserId = userDao.findByUsername(currentUser.getName()).getId();

        // Get the transfer by ID
        Transfer transfer = transferDao.getTransferByTransferId(id, currentUserId);

        // Check if the transfer exists
        if (transfer != null) {
            return transfer;
        } else {
            // The transfer does not exist
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "You have no transfers with this transfer ID!");
        }
    }

    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(path="/request", method=RequestMethod.PUT)
    public String createRequest(@Valid @RequestBody Transfer transfer, Principal principal) {
        // Get the ID of the current user
        currentUserId = userDao.findByUsername(principal.getName()).getId();

        // Get the sending and receiving accounts
        Account sendingAccount = accountDao.getAccountById(transfer.getSending_account_id());
        Account receivingAccount = accountDao.getAccountById(transfer.getReceiving_account_id());

        // Check if the current user is the owner of the receiving account
        if (currentUserId == receivingAccount.getUserId()) {

            // Check if the sending and receiving accounts belong to different users
            if (sendingAccount.getUserId() != receivingAccount.getUserId()) {

                // Get a map of all users with their ID and name
                Map<Integer, String> allUsers = userDao.findAllUserIdAndName(currentUserId);

                // Check if the user associated with the sending account exists in the map
                if (allUsers.containsKey(sendingAccount.getUserId())) {

                    // Make the transfer request
                    transferDao.requestTransfer(sendingAccount, receivingAccount, transfer.getTransfer_amount());
                    return "Request sent";
                }
                // If the user associated with the sending account does not exist, throw exception
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist");
            }
            // If the sending and receiving accounts belong to the same user, throw exception
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot request money from yourself");
        }
        // If the current user is not the owner of the receiving account, throw exception
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot make the request!");
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping(path = "/request/approve/{id}", method = RequestMethod.POST)
    public String approveRequest(@Valid @PathVariable int id, Principal currentUser) {
        // Get the current user's ID
        currentUserId = userDao.findByUsername(currentUser.getName()).getId();

        // Get the transfer and the sending and receiving accounts
        Transfer transfer = transferDao.getTransferByTransferId(id, currentUserId);
        Account sendingAccount = accountDao.getAccountById(transfer.getSending_account_id());
        Account receivingAccount = accountDao.getAccountById(transfer.getReceiving_account_id());

        // Check if the transfer exists
        if (transferDao.getTransferByTransferId(id, currentUserId) != null) {
            // Check if the transfer is a pending request
            if(transfer.getTransfer_status().equals("Pending")) {
                // Check if the current user has sufficient funds to approve the request
                if (accountDao.getFunds(currentUser.getName()).compareTo(transfer.getTransfer_amount()) != -1) {
                    // Get a map of all users and their IDs
                    currentUserId = sendingAccount.getUserId();
                    Map<Integer, String> allUsers = userDao.findAllUserIdAndName(currentUserId);

                    // Check if the receiving account belongs to a known user
                    if (allUsers.containsKey(receivingAccount.getUserId())) {
                        // Approve the request
                        transferDao.respondToTransferRequest(id, sendingAccount, receivingAccount, transfer.getTransfer_amount(), true);
                        return "Request Approved";
                    } else {
                        // The receiving account does not belong to a known user
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist.");
                    }
                } else {
                    // The current user does not have sufficient funds to approve the request
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request Cannot Be Accepted With Current Funds!");
                }
            } else {
                // The transfer is not a pending request
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a request!");
            }
        } else {
            // The transfer does not exist
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer does not exist");
        }
    }

    @ResponseStatus(HttpStatus.ACCEPTED)
    @RequestMapping(path = "/request/reject/{id}", method = RequestMethod.POST)
    public String rejectRequest(@Valid @PathVariable int id, Principal currentUser) {
        // Get the current user's ID
        currentUserId = userDao.findByUsername(currentUser.getName()).getId();

        // Get the transfer and the sending and receiving accounts
        Transfer transfer = transferDao.getTransferByTransferId(id, currentUserId);
        Account sendingAccount = accountDao.getAccountById(transfer.getSending_account_id());
        Account receivingAccount = accountDao.getAccountById(transfer.getReceiving_account_id());

        // Check if the transfer exists
        if (transferDao.getTransferByTransferId(id, currentUserId) != null) {
            // Check if the transfer is a pending request
            if (transfer.getTransfer_status().equals("Pending")) {
                    // Get a map of all users and their IDs
                    currentUserId = sendingAccount.getUserId();
                    Map<Integer, String> allUsers = userDao.findAllUserIdAndName(currentUserId);

                    // Check if the receiving account belongs to a known user
                    if (allUsers.containsKey(receivingAccount.getUserId())) {
                        // Reject the request
                        transferDao.respondToTransferRequest(id, sendingAccount, receivingAccount, transfer.getTransfer_amount(), false);
                        return "Request Rejected";
                    } else {
                        // The receiving account does not belong to a known user
                        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User does not exist.");
                    }
            } else {
                // The transfer is not a pending request
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not a request!");
            }
        } else {
            // The transfer does not exist
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Transfer does not exist");
        }
    }

    @RequestMapping(path= "/request/list", method= RequestMethod.GET)
    public List<Transfer> getMyRequestList(Principal currentUser){
        // Get the current user's ID
        currentUserId = userDao.findByUsername(currentUser.getName()).getId();

        // Get all requests for the current user
        return transferDao.getAllRequest(currentUserId);
    }
}
