package application;

import framework.Service;

@Service("accountService")
public class AccountService implements IAccountService{
    public int getAccountNumber(){
        return 9000;
    }


}
