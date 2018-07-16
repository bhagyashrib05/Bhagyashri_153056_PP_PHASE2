package com.cg.mypaymentapp.repo;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import com.cg.mypaymentapp.beans.Customer;
import com.cg.mypaymentapp.beans.Transactions;
import com.cg.mypaymentapp.beans.Wallet;
import com.cg.mypaymentapp.exception.InsufficientBalanceException;
import com.cg.mypaymentapp.exception.InvalidInputException;
import com.cg.mypaymentapp.util.DBUtil;

public class WalletRepoImpl implements WalletRepo 
{
	
	
	public WalletRepoImpl()
	{
		
	}

	
	@Override
	public boolean save(Customer customer) 
	{
		try(Connection con=DBUtil.getConnection())
		{
			try
			{
			PreparedStatement pstm=con.prepareStatement("insert into Wallet values(?,?,?)");
			
			pstm.setString(1, customer.getName());
			pstm.setString(2, customer.getMobileNo());
			pstm.setBigDecimal(3, customer.getWallet().getBalance());
			
			
			pstm.execute();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		catch(Exception e)
		{
			throw new InvalidInputException(e.getMessage());
		}
		return true;

}

	@Override
	public Customer findOne(String mobileNo) 
	{
		Customer cust=null;
		
		try(Connection con=DBUtil.getConnection())
		{
		
			PreparedStatement pstm=con.prepareStatement("select * from Wallet where MobileNo=?");
			
			pstm.setString(1, mobileNo);
			
			ResultSet res=pstm.executeQuery();
			
			if(res.next()==false)
				throw new InvalidInputException("No customer with this mobile number "+mobileNo);
			
			cust=new Customer();
			
			cust.setName(res.getString(1));
			cust.setMobileNo(res.getString(2));
			Wallet wallet=new Wallet(res.getBigDecimal(3));
			cust.setWallet(wallet);
			
		}
		catch(Exception e)
		{
			throw new InvalidInputException(e.getMessage());
		}
		
		return cust;
		
		
	}
	
	public Customer depositAmount(String mobileNo, BigDecimal amount)
	{
		
		Customer cust=new Customer();
		try(Connection con=DBUtil.getConnection())
		{
			PreparedStatement pstm=con.prepareStatement("select * from Wallet where MobileNo=?");
			
			pstm.setString(1, mobileNo);
			
			ResultSet res=pstm.executeQuery();
			
			if(res.next()==false)
				throw new InvalidInputException("No customer with this mobile number "+mobileNo);
			
			
			
			cust.setName(res.getString(1));
			cust.setMobileNo(res.getString(2));
			Wallet wallet=new Wallet(res.getBigDecimal(3));
			
			BigDecimal bal=wallet.getBalance();
			bal=bal.add(amount);
			Wallet wallet1=new Wallet(bal);
			cust.setWallet(wallet1);
		
			 pstm=con.prepareStatement("Update Wallet set Amount=? where MobileNo=?");
			
			 pstm.setBigDecimal(1, bal);
			pstm.setString(2, mobileNo);
			
			pstm.executeUpdate();
			
			
			Transactions transactions=new Transactions();
			transactions.setDescription(amount+" was credited\t\t");
			transactions.setCredited(amount);
			
			pstm=con.prepareStatement("insert into Transactions values(?,?,0,?)");
			
			pstm.setString(1, mobileNo);
			pstm.setString(2, transactions.getDescription());
			pstm.setBigDecimal(3, transactions.getCredited());
			
			
			pstm.execute();
			
			
		}
		catch(Exception e)
		{
			throw new InvalidInputException(e.getMessage());
		}
		return cust;
		
	}
	
	public Customer withdrawAmount(String mobileNo, BigDecimal amount)
	{
		Customer cust=new Customer();
		try(Connection con=DBUtil.getConnection())
		{
			PreparedStatement pstm=con.prepareStatement("select * from Wallet where MobileNo=?");
			
			pstm.setString(1, mobileNo);
			
			ResultSet res=pstm.executeQuery();
			
			if(res.next()==false)
				throw new InvalidInputException("No customer with this mobile number "+mobileNo);
			
			
			
			cust.setName(res.getString(1));
			cust.setMobileNo(res.getString(2));
			Wallet wallet=new Wallet(res.getBigDecimal(3));
			
			BigDecimal bal=wallet.getBalance();
			if(bal.compareTo(amount)>0)
			bal=bal.subtract(amount);
			else
				throw new InsufficientBalanceException("Insufficient balance");
			
			Wallet wallet1=new Wallet(bal);
			cust.setWallet(wallet1);
		
			 pstm=con.prepareStatement("Update Wallet set Amount=? where MobileNo=?");
			
			 pstm.setBigDecimal(1, bal);
			pstm.setString(2, mobileNo);
			
			pstm.executeUpdate();
			
			Transactions transactions=new Transactions();
			transactions.setDescription(amount+" was debited");
			transactions.setDebited(amount);
			
			pstm=con.prepareStatement("insert into Transactions values(?,?,?,0)");
			
			pstm.setString(1, mobileNo);
			pstm.setString(2, transactions.getDescription());
			pstm.setBigDecimal(3, transactions.getDebited());
			
			
			pstm.execute();
		}
		catch(Exception e)
		{
			throw new InvalidInputException(e.getMessage());
		}
		return cust;
	}
	
	public Customer fundTransfer(String sourceMobileNo, String targetMobileNo, BigDecimal amount)
	{
		Customer cust=new Customer();
		try(Connection con=DBUtil.getConnection())
		{
			PreparedStatement pstm=con.prepareStatement("select * from Wallet where MobileNo=?");
			
			pstm.setString(1, sourceMobileNo);
			
			ResultSet res=pstm.executeQuery();
			
			if(res.next()==false)
				throw new InvalidInputException("No customer with this mobile number "+sourceMobileNo);
			
			
			
			cust.setName(res.getString(1));
			cust.setMobileNo(res.getString(2));
			Wallet wallet=new Wallet(res.getBigDecimal(3));
			
			BigDecimal bal=wallet.getBalance();
			if(bal.compareTo(amount)>0)
			bal=bal.subtract(amount);
			else
				throw new InsufficientBalanceException("Insufficient balance");
			
			Wallet wallet1=new Wallet(bal);
			cust.setWallet(wallet1);
		
			 pstm=con.prepareStatement("Update Wallet set Amount=? where MobileNo=?");
			
			 pstm.setBigDecimal(1, bal);
			pstm.setString(2, sourceMobileNo);
			
			pstm.executeUpdate();
			
			
			
			Transactions transactions=new Transactions();
			transactions.setDescription(amount+" was transferred to "+targetMobileNo);
			transactions.setDebited(amount);
			
			pstm=con.prepareStatement("insert into Transactions values(?,?,?,0)");
			
			pstm.setString(1, sourceMobileNo);
			pstm.setString(2, transactions.getDescription());
			pstm.setBigDecimal(3, transactions.getDebited());
			
			
			pstm.execute();
			
			
			
			pstm=con.prepareStatement("select * from Wallet where MobileNo=?");
			
			pstm.setString(1, targetMobileNo);
			
			 res=pstm.executeQuery();
			
			if(res.next()==false)
				throw new InvalidInputException("No customer with this mobile number "+targetMobileNo);
			
			
			
			cust.setName(res.getString(1));
			cust.setMobileNo(res.getString(2));
			Wallet wallet2=new Wallet(res.getBigDecimal(3));
			
			BigDecimal bal2=wallet2.getBalance();
			bal2=bal2.add(amount);
			Wallet wallet3=new Wallet(bal2);
			cust.setWallet(wallet3);
		
			 pstm=con.prepareStatement("Update Wallet set Amount=? where MobileNo=?");
			
			 pstm.setBigDecimal(1, bal2);
			pstm.setString(2, targetMobileNo);
			
			pstm.executeUpdate();
			
			
			
			transactions.setDescription(amount+" received from "+sourceMobileNo);
			transactions.setCredited(amount);
			
			pstm=con.prepareStatement("insert into Transactions values(?,?,0,?)");
			
			pstm.setString(1, targetMobileNo);
			pstm.setString(2, transactions.getDescription());
			pstm.setBigDecimal(3, transactions.getCredited());
			
			
			pstm.execute();
		}
		catch(Exception e)
		{
			throw new InvalidInputException(e.getMessage());
		}
		return cust;
	}
	
	
public Transactions showTransactions(String mobileNo)
{
	Transactions trans=new Transactions();
	try(Connection con=DBUtil.getConnection())
	{
		PreparedStatement pstm=con.prepareStatement("select * from Transactions where MobileNo=?");
		
		pstm.setString(1, mobileNo);
		
		ResultSet res=pstm.executeQuery();
		
		if(res.next()==false)
			throw new InvalidInputException("No customer with this mobile number "+mobileNo);
		
		System.out.println("Description");
		System.out.println(res.getString(2));
		while(res.next())
		{
		System.out.print(res.getString(2) );
		
		System.out.println();
		
		}
		
}

	catch(Exception e)
	{
		throw new InvalidInputException(e.getMessage());
	}
	return trans;
	
}
	

}
