CS 848 - Deduplication in RDF
=============================

The purpose of the project is to demonstrate the efficacy of graph embeddings for detecting similar nodes in an RDF graph. The code here is made available so that our experiments could be easily replicated.

## Datasets
The datasets that we use are available at:
* [ground truth](https://hpi.de/naumann/projects/repeatability/datasets/dblp-dataset.html)
* [dblp RDF graph](https://old.datahub.io/dataset/l3s-dblp)

## Project structure
The code is divided into folders by purpose. 
* Data - contains code for generating the intermediate datasets from the original data in a form that can be consumed by the rest of the code.
* Baseline - contains the code for running the baseline.
* Graph Embeddings - contains the code for creating graph embeddings.
* Classification - contains the code for creating the classifier model and getting classification results for node pairs.

## Running the experiments
Creating datasets.
* Create a smaller RDF dataset from the large `dblp_RDF_GRAPH` such that only the neighborhood information of the ground truth nodes are available.
Run `data\crawler.ipynb`  
Input: `ground truth` and `dblp RDF graph` files.  
Output: `dblp_dataset_*.nt` file containing the smaller RDF dataset. `dblp_*.json` file containing a dictionary of ground truth data that was used in creating the smaller dataset.  

* Create a list of node pairs along with labelling information.
Run `data\create_edgelist.py`  
Input `dblp_*.json` file from the previous step.  
Output `dblp_*.edges`, `dblp_*.authors`, `dblp_*.papers` files.  `dblp_*.edges` contains the labelled data along with node ids. The other files map node ids to nodes in the RDF graph.

## Running the baseline
Run `baseline\run_baseline.ipynb`  
Input `dblp_dataset_*.nt` file and `dblp_*.edges` denoting labelling information.  
Output: Accuracy information of the baseline.

## Generating graph embeddings
Refer the instructions at [graph embedding generation](./Graph_Embeddings/RDF2VEC/README.md)

## Classification
Refer the instructions at [classification](./Classification/README.md)