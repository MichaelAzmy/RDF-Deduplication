'''
Created on Feb 16, 2016

@author: petar
'''
# import modules; set up logging
import gensim, logging, os, sys
logging.basicConfig(format='%(asctime)s : %(levelname)s : %(message)s',filename='word2vec.out', level=logging.INFO)
class MySentences(object):
	def __init__(self, dirname):
		self.dirname = dirname

	def __iter__(self):
		for fname in os.listdir(self.dirname):
			try:
				for line in open(os.path.join(self.dirname, fname), mode='rt'):
					line = line.rstrip('\n')
					words = line.split("->")[0:-2]
					yield words
			except Exception:
				print("Failed reading file:")
				print(fname)
sentences = MySentences("walks") # a memory-friendly iterator
#sg 500
model = gensim.models.Word2Vec(sentences=sentences, size=500, workers=5, window=10, sg=1, negative=15, iter=5)
#model.build_vocab(sentences)
#model.train(sentences)
#sg/cbow features iterations window negative hops random walks
model.save('DBLP_sg_500_5_5_15_2_500')

