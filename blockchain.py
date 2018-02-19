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
from textwrap import dedent
from uuid import uuid4
from urllib.parse import urlparse

from flask import Flask, jsonify, request

class Blockchain(object):
    def __init__(self):
        self.chain = []
        self.current_transactions = []
        self.nodes = set()


        # Create new genesis block
        self.new_block(previous_hash=1, proof=100)

    def register_node(self, address):
        '''

        Add a new node to the list of nodes

        :param address: <str> Address of node. Eg. 'http://192.168.0.5:5000'
        :return: None
        '''


        parsed_url = urlparse(address)
        self.nodes.padd(parsed_url.netloc)
        
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

    @staticmethod
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

    def valid_chain(self, chain):
        '''

        Determine if a given blockchain is valid

        :param chain: <list> A blockchain
        :return: <bool> True if valid, False if not

        '''

        last_block = chain[0]
        current_index = 1

        while current_index < len (chain):
            block = chain[current_index]
            print (f'{last_block}')
            print (f'{block}')
            print ('\n-------------------\n')

            #Check that the hash of the block is correct

            if block['previous_hash'] != self.hash(last_block):
                return False

            last_block = block
            current_index += 1

        return True

    def resolve_conflicts(self):
        '''

        This is our Consensus Algo, resolves conflicts by replacing our chain with 
        longest one in network.

        :return: <bool> True if our chain was replaced, False if not

        '''

        neighbors = self.nodes
        new_chain = None

        #We're only looking for chains longer than ours
        max_length = len(self.chain)

        #Grab and verify chains from all nodes in network
        for node in neighbors:
                response = requests.get(f'http://{node}/chain')

                if response.status_code == 200:
                    length = response.json()['length']
                    chain = response.json()['chain']

                    # Check if length is longer than chain is valid
                    if length > max_length and self.valid_chain(chain):
                        max_length = length
                        new_chain = chain

        if new_chain:
            self.chain = new_chain
            return True

        return False

# Instantiate our Node
app = Flask(__name__)

# Generate globally unique address for this node
node_identifier = str(uuid4()).replace('-', '')

# Instantiate the Blockchain
blockchain = Blockchain()

@app.route('/mine', methods=['GET'])
def mine():
    # We run PoW algo to get to the next proof...
    last_block = blockchain.last_block
    last_proof = last_block['proof']
    proof = blockchain.proof_of_work(last_proof)

    # We must receive reward for finding that proof
    # Sender is "0" to signify that this node has mined a new coin

    blockchain.new_transaction(
        sender ='0',
        recipient = node_identifier,
        amount = 1 
        )

    # Forge the new Block by adding it to the chain
    previous_hash = blockchain.hash(last_block)
    block = blockchain.new_block(proof, previous_hash)

    response = {
        'message': "New Block Forged",
        'index': block['index'],
        'transactions': block['transactions'],
        'proof': block['proof'],
        'previous_hash': block['previous_hash'],
    }

    return jsonify(response), 200


@app.route('/chain', methods=['Get'])
def full_chain():
    response = {
        'chain': blockchain.chain,
        'length': len(blockchain.chain),
    }
    return jsonify(response), 200

@app.route('/transactions/new', methods=['POST'])
def new_transaction():
    values = request.get_json()

    #Check that the required field are in the POST'ed data
    required = ['sender', 'recipient', 'amount']
    if not all(k in values for k in required):
        return 'Missing values', 400

    # Create a new Transaction
    index = blockchain.new_transaction(values['sender'], values['recipient'], values['amount'])

    response = {'message': f'Transaction will be added to Block {index}'}
    return jsonify(response), 201

@app.route('/nodes/register', methods = ['POST'])
def register_nodes():
    values = request.get_json()

    nodes = values.get('nodes')
    if nodes is None:
        return "Error: Please supply a valid list ode nodes", 400

    for node in nodes:
        blockchain.register_node(node)

    response = {
        'message': 'New nodes have been added',
        'total_nodes': list(blockchain.nodes),

    }
    return jsonify(response), 201

@app.route('/nodes/resolve', methods=['GET'])
def consensus():
    replaced = blockchain.resolve_conflicts()

    if replaced:
        response = {
            'message': 'Our chain was replaced',
            'new_chain': blockchain.chain
        }

    else:
        response = {
            'message': 'Our chain is authoritative',
            'chain': blockchain.chain
        }

    return jsonify(response), 200

'''
def main():

    new_blockchain = Blockchain()
    BC_chain = new_blockchain.chain

    BC_len = len(new_blockchain.chain)   


    while BC_len != 10:
        new_blockchain.new_block(proof = new_blockchain.chain[-1]['proof'] + BC_len)
        BC_len = len(new_blockchain.chain) 

    print (json.dumps(new_blockchain.chain, indent = 4))
'''

if __name__ == "__main__": 
    #main()
    app.debug = True
    app.run(host='127.0.0.1', port=5000)



