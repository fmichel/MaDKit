/*
Copyright or © or Copr. Jason Mahdjoub (04/02/2016)

jason.mahdjoub@distri-mind.fr

This software (Utils) is a computer program whose purpose is to give several kind of tools for developers 
(ciphers, XML readers, decentralized id generators, etc.).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.kernel.network.connection;



import com.distrimind.madkit.exceptions.BlockParserException;
import com.distrimind.madkit.exceptions.PacketException;
import com.distrimind.madkit.kernel.network.Block;
import com.distrimind.madkit.kernel.network.SubBlock;
import com.distrimind.madkit.kernel.network.SubBlockInfo;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadKitLanEdition 1.4.0
 */
public class PointToPointTransferedBlockChecker extends TransferedBlockChecker {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3951498788798655329L;
	private transient ConnectionProtocol<?> cpInput=null, cpOutput=null;

	public PointToPointTransferedBlockChecker() {
		super(null, false);
	}
	
	@Override
	public SubBlockInfo checkSubBlock(SubBlock _block) throws BlockParserException {
		if (cpInput==null)
			throw new NullPointerException();
		
		try
		{

			SubBlock sb=_block;
			if (_block.getOffset()!=Block.getHeadSize())
				throw new IllegalAccessError();
			
			for (java.util.Iterator<ConnectionProtocol<?>> it=cpInput.iterator();it.hasNext();)
			{
				ConnectionProtocol<?> cp=it.next();
				//cp.getPacketCounter().incrementMyCounters();
				SubBlockInfo checkedBlock=cp.getParser().checkEntrantPointToPointTransferedBlock(sb);
				if (!checkedBlock.isValid())
					return checkedBlock;
				sb=checkedBlock.getSubBlock();
			}
			if (cpOutput==null)
			{
				if (sb.getOffset()==Block.getHeadSize())
					return new SubBlockInfo(sb, true, false);
				SubBlock res=new SubBlock(new Block(sb.getSize()+Block.getHeadSize(), Block.getTransferID(_block.getBytes())));
				System.arraycopy(sb.getBytes(), sb.getOffset(), res.getBytes(), res.getOffset(), sb.getSize());
				//Block.setCounterState(res.getBytes(), Block.getCounterState(_block.getBytes()));
				return new SubBlockInfo(res, true, false);
			}
			return new SubBlockInfo(prepareBlock(new SubBlock(sb.getBytes(), Block.getHeadSize(), sb.getBytes().length-Block.getHeadSize()), sb.getOffset()-Block.getHeadSize(), new Block(_block.getBytes()).getTransferID()), true, false);
		}
		catch (PacketException e)
		{
			throw new IllegalAccessError(e.getMessage());
		}
	}

	private SubBlock prepareBlock(SubBlock _block, int totalInputHeadSize, int transferType) throws BlockParserException, PacketException
	{
		if (cpOutput==null)
			throw new NullPointerException();
		
		int totalOutputHeadSize=0;
		
		for (java.util.Iterator<ConnectionProtocol<?>> it=cpOutput.iterator();it.hasNext();)
		{
			ConnectionProtocol<?> cp=it.next();
			totalOutputHeadSize+=cp.getParser().getSizeHead();
		}
		SubBlock res=null;
		if (_block.getOffset()!=Block.getHeadSize())
			throw new IllegalAccessError();
		if (totalInputHeadSize>=totalOutputHeadSize)
		{
			res=new SubBlock(_block.getBytes(), Block.getHeadSize()+totalOutputHeadSize, _block.getBytes().length-Block.getHeadSize()-totalInputHeadSize);
		}
		else
		{
			res=new SubBlock(new Block(_block.getBytes().length-totalInputHeadSize+totalOutputHeadSize, transferType));
			res=new SubBlock(res.getBytes(), res.getOffset()+totalOutputHeadSize, _block.getBytes().length-Block.getHeadSize()-totalInputHeadSize);
			System.arraycopy(_block.getBytes(), Block.getHeadSize()+totalInputHeadSize, res.getBytes(), res.getOffset(), res.getSize());
			//Block.setCounterState(res.getBytes(), Block.getCounterState(_block.getBytes()));
		}
		
		for (java.util.Iterator<ConnectionProtocol<?>> it=cpOutput.reverseIterator();it.hasNext();)
		{
			ConnectionProtocol<?> cp=it.next();
			//cp.getPacketCounter().incrementOtherCounters();
			res=cp.getParser().signIfPossibleSortantPointToPointTransferedBlock(res);
		}
		
		return res;
	}
	
	
	public void setConnectionProtocolInput(ConnectionProtocol<?> cpInput)
	{
		this.cpInput=cpInput;
	}
	
	public void setConnectionProtocolOutput(ConnectionProtocol<?> cpOuput)
	{
		this.cpOutput=cpOuput;
	}
	
	
	
	public SubBlock prepareBlockToSend(SubBlock _block) throws BlockParserException, PacketException
	{
		return prepareBlock(_block, 0, -1);
	}

	@Override
	public int getInternalSerializedSize() {
		return 0;
	}

	

}
