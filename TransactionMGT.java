package test;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class TransactionMGT
{
	public static void main(String[] args)
	{

		try
		{
			Class.forName("oracle.jdbc.OracleDriver");
			Connection con=DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:xe","anshul","xyz");
			Scanner s=new Scanner(System.in);

//			boolean b=con.getAutoCommit();
//			System.out.print(b);

			con.setAutoCommit(false);

//			boolean a=con.getAutoCommit();
//			System.out.print(a);

			while(true)
			{
				System.out.println("Operations on Bank");
				System.out.println("===================");
				System.out.println("\t1.Check Balance");
				System.out.println("\t2.Transfer Money");
				System.out.println("\t3.Deposit Money");
				System.out.println("\t4.WithDraw Money");
				System.out.println("\t5.View Transfer Trasaction");
				System.out.println("\t6.View Deposit/Withdraw Transaction");
				System.out.println("\t7.Exit");

				System.out.println("Enter Choice: ");
				int choice=s.nextInt();

				switch(choice)
				{
					case 1:
					{
						PreparedStatement ps1=con.prepareStatement("select balance from SBIBank where acno=?");
						System.out.println("Enter your Account no: ");
						long uacno=s.nextLong();
						ps1.setLong(1, uacno);
						ResultSet rs1=ps1.executeQuery();
						if(rs1.next())
						{
							float ubal=rs1.getFloat(1);
							System.out.println("Your Current Balance: "+ubal);
						}
						else
						{
							System.out.println("Please Check Your Account no.");
						}
					}
					break;
					case 2:
					{
						PreparedStatement ps2=con.prepareStatement("select balance from SBIBank where acno=?");
						PreparedStatement ps3=con.prepareStatement("update SBIBank set balance=balance+? where acno=?");

						Savepoint sp=con.setSavepoint();

						System.out.println("Enter Account no From Which You want to Transfer Money: ");
						long facno=s.nextLong();
						ps2.setLong(1, facno);
						ResultSet rs2=ps2.executeQuery();
						if(rs2.next())
						{
							float bl=rs2.getFloat(1);
							System.out.println("Enter Account no On which You want to Get Money: ");
							long tacno=s.nextLong();
							ps2.setLong(1, tacno);
							ResultSet rs3=ps2.executeQuery();
							if(rs3.next())
							{
								System.out.println("Enter Money You Want to Transfer: ");
								float tmoney=s.nextFloat();
								//if(bl<=tmoney)
								//{
									ps3.setFloat(1, -tmoney);
									ps3.setLong(2, facno); //From Account
									int i=ps3.executeUpdate();

									ps3.setFloat(1,tmoney);
									ps3.setLong(2, tacno);
									int j=ps3.executeUpdate();

									if(i==1 && j==1)
									{
										System.out.println("Transaction Successfull");

										String currentTime = new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());

										PreparedStatement ps5=con.prepareStatement("insert into SBITransaction (Fromacno,Toaccount,payment,ptime) values (?,?,?,?)");
										ps5.setLong(1, facno);
										ps5.setLong(2, tacno);
										ps5.setFloat(3, tmoney);
										ps5.setString(4, currentTime);
										int a=ps5.executeUpdate();

										con.commit();
									}
									else
									{
										System.out.println("Transaction Failed");
										con.rollback(sp);
									}
								//}
								//else
								//{
									//System.out.println("Insufficient Balance");
								//}
							}
							else
							{
								System.out.println("Invalid Account number");
							}
						}
						else
						{
							System.out.println("Invalid Account number");
						}
					}
					break;
					case 3:
					{
						PreparedStatement ps6=con.prepareStatement("update sbibank set BALANCE=BALANCE+? where ACNO=?");
						System.out.println("Enter Account no: ");
						long dacno=s.nextLong();
						System.out.println("Enter Money to Deposit: ");
						float damt=s.nextFloat();
						ps6.setFloat(1, damt);
						ps6.setLong(2, dacno);
						int c=ps6.executeUpdate();
						if(c>0)
						{
							System.out.println(damt+" Deposited successfully");

							PreparedStatement ps9=con.prepareStatement("insert into sbidepwithTransaction (acno,type,amt,dwdate,time) values(?,?,?,?,?)");
							String ptype="Deposit";
							String pcurrenttime=new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
							ps9.setLong(1, dacno);
							ps9.setString(2, ptype);
							ps9.setFloat(3, damt);
							ps9.setDate(4, new java.sql.Date(System.currentTimeMillis())); // For dwdate, pass the current date
							ps9.setString(5, pcurrenttime);

							int d=ps9.executeUpdate();

							con.commit();
						}
						else
						{
							System.out.println("No Account Found");
						}
					}
					break;
					case 4:
					{
						PreparedStatement ps7=con.prepareStatement("update sbibank set BALANCE=BALANCE-? where ACNO=?");
						System.out.println("Enter Account no: ");
						long wacno=s.nextLong();
						System.out.println("Enter Money to Withdraw: ");
						float wamt=s.nextFloat();
						ps7.setFloat(1, wamt);
						ps7.setLong(2, wacno);
						PreparedStatement ps8=con.prepareStatement("select BALANCE from sbibank where acno=?");
						ps8.setLong(1, wacno);
						ResultSet rs5=ps8.executeQuery();
						if(rs5.next())
						{
							float total=rs5.getFloat(1);
							if(wamt<=total)
							{
								int e=ps7.executeUpdate();
								if(e>0)
								{
									System.out.println(wamt+" Withdraw Successfully");

									PreparedStatement ps10=con.prepareStatement("insert into sbidepwithTransaction (acno,type,amt,DWDATE,Time) values(?,?,?,?,?)");
									String ptype="Withdraw";
									String pcurrenttime=new SimpleDateFormat("HH:mm:ss").format(new java.util.Date());
									ps10.setLong(1, wacno);
									ps10.setString(2, ptype);
									ps10.setFloat(3, wamt);
									ps10.setDate(4, new java.sql.Date(System.currentTimeMillis())); // For dwdate, pass the current date
									ps10.setString(5, pcurrenttime);

									int f=ps10.executeUpdate();

									con.commit();
								}
							}
							else
							{
								System.out.println("Insufficient Balance");
							}
						}
						else
						{
							System.out.println("No Account Found");
						}

					}
					break;
					case 5:
					{
						PreparedStatement ps11=con.prepareStatement("select *from sbitransaction");
						ResultSet rs6=ps11.executeQuery();
						System.out.println("FromAcno ToAcno\tPayment\tPDate\t\tPTime");
						while(rs6.next())
						{
							System.out.println(rs6.getLong(1)+"\t"+rs6.getLong(2)+"\t"+rs6.getFloat(3)+"\t"+rs6.getDate(4)+"\t"+rs6.getString(5));
						}
					}
					break;
					case 6:
					{
						PreparedStatement ps12=con.prepareStatement("select *from sbidepwithTransaction");
						ResultSet rs7=ps12.executeQuery();
						System.out.println("Acno\tType\tAmount\tDate\t\tTime");
						while(rs7.next())
						{
							System.out.println(rs7.getLong(1)+"\t"+rs7.getString(2)+"\t"+rs7.getFloat(3)+"\t"+rs7.getDate(4)+"\t"+rs7.getString(5));
						}
					}
					break;
					case 7:
					{
						System.out.println("Operations Stopped");
						System.exit(0);
					}
					default:
						System.out.println("Invalid Choice");
				}
			}
		}
		catch(Exception e)
		{
			System.out.println(e.getMessage());
		}
	}

}
