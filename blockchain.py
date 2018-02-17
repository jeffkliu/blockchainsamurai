'''This class is used to create a blockchain 
    Example of what blockchain looks like: block = {
    'index': 1,
    'timestamp': 1506057125.900785,
    'transactions': [
        {
            'sender': "8527147fe1f5426f9dd545de4b27ee00",
            'recipient': "a77f5cdfa2934df3954a5c7c7da5df1f",
            'amount': 5,
        }
    ],
    'proof': 324984774000,
    'previous_hash': "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824"
}
'''
import hashlib
import json
from time import time

class Blockchain(object):
    def __init__(self):
        self.chain = []
        self.current_transactions = []


        # Create new genesis block
        self.new_block(previous_hash=1, proof=100)
        
    def new_block(self, proof, previous_hash = None):
        '''

        Create new Block in the Blockchain

        :param proof: <int> The proof given by the PoW algo
        :param previous_hash: (Optional) <str> HAsh of previous Block
        :return: <dict> New Block

        '''

        block = {
            'index': len(self.chain) + 1,
            'timestamp': time(),
            'transactions': self.current_transactions,
            'proof': proof,
            'previous_hash': previous_hash or self.hash(self.chain[-1]),


        }

        self.current_transactions = []

        self.chain.append(block)

        return block
        
    
    def new_transaction(self, sender, recipient, amount):
        '''

        Creates a new transaction to go into the next mined Block

        :param sender: <str> Address of Sender
        :param recipient: <str> Address of Recipient
        :param amount: <int> Amount
        :return <int> The index of the Block that will hold this transaction

        '''

        self.current_transactions.append(
            {
                'sender': sender,
                'recipient': recipient,
                'amount': amount,
            }
            )
            
        return self.last_block['index'] + 1
    
    @staticmethod
    def hash(block):
        '''
        
        Creates a SHA-256 hash of a Block

        :param block: <dict> Block
        :return <str>
        

        '''

        # We must make sure that the Ditionary is Ordered, or we'll have inconsistent hashes
        block_string = json.dumps(block, sort_keys=True).encode()
        return hashlib.sha256(block_string).hexdigest()


    @property
    def last_block(self):
        return self.chain[-1]

    def proof_of_work(self, last_proof):
        '''

        Simple PoW algo:
        - Find a number p' such that hash(pp') contains leading 4 zeroes, where p is 
        previous p'
        - p is previous proof, and p' is the new proof

        :param last_proof: <int>
        :retuen: <int>

        '''

        proof = 0
        while self.valid_proof(last_proof, proof) is False:
            proof += 1

        return proof

    def valid_proof(last_proof, proof):
        '''

        Validates the Proof: Does hash(last_proof, proof) contain 4 leading zeroes?

        :param last_proof: <int> Previous Proof
        :para proof: <int> Current Proof
        :return: <bool> True if correct, False if not
        '''

        guess = f'{last_proof}{proof}'.encode()
        guess_hash = hashlib.sha256(guess).hexdigest()
        return guess_hash[:4] == '0000'

def main():

    new_blockchain = Blockchain()
    BC_chain = new_blockchain.chain

    BC_len = len(new_blockchain.chain)   


    while BC_len != 10:
        new_blockchain.new_block(proof = new_blockchain.chain[-1]['proof'] + BC_len)
        BC_len = len(new_blockchain.chain) 

    print json.dumps(new_blockchain.chain, indent = 4)

if __name__ == "__main__": main()



