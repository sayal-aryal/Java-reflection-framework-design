package application;

import framework.Service;

@Service("accountServiceDemo")
public class AccountServiceDemo implements IAccountService{
    public int getAccountNumber(){
        return 1111;
    }
}
