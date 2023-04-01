> CITYTRACKS is App for collecting and analysing urban mobility pattern. 
it was used in my MSc. in Information Systems at UNIRIO.

> Detecting the transportation mode for context-aware systems using smartphones: https://dl.acm.org/doi/10.1109/ITSC.2016.7795921

> Languages: Objective-C, PHP, R, Java.

---

# CityTracks: Leveraging Mobility Data to Identify Urban Mobility Patterns and Modes #

## Introduction ##

Urban mobility is a key challenge for city planners and policymakers, as it impacts many aspects of urban life, from traffic congestion to air pollution. In recent years, mobile sensing technologies have enabled the collection of large amounts of mobility data, providing new opportunities for analyzing and understanding urban mobility patterns and modes. CityTracks is a mobile application designed to collect mobility data and identify mobility patterns and modes in urban populations. This white paper provides an overview of the data collection process and techniques used in the CityTracks research project.

## Data Collection in Real Usage ##

To ensure greater control over the quality of the information collected, the CityTracks research project decided to collect its own data, rather than relying on any available trace repository. This decision provided additional research opportunities, including the investigation of mobility aspects of a well-known area. To support the data collection, the project team designed the infrastructure for the server side and for the client app. A framework called S3A was developed to provide guidelines and blueprints for the infrastructure of generic participatory sensing researches.

The data was collected from 9 volunteers, who were in full control of the data they were putting available. The data collection effort resulted in over 880,000 geo-location records, which were acquired from 180 trajectories, covering various transportation modes such as car, bus, bike, walking, and motorcycle. The data collection covered the South Zone, North Zone, Downtown of Rio de Janeiro, South Zone, North Zone, Central and Oceanic Region of Niteroi, as shown in Figure 3.

## Trajectory Representation ##

The use of collected raw data has little value for data mining, requiring interpretation and feature extraction steps before the data can be used. The process of estimating a path through smartphones is a difficult problem because there is no embedded sensor that is accurate in all locations. A pre-processing step after the data collection was applied to group related positional records into trajectories. Each trajectory can have periods of movement and periods of pause (stops) identified, and each location record is associated with a single specific trajectory segment or a single pause.

## Applied Techniques to Evaluate the Classifier Algorithms ##

To evaluate the classifier algorithms' performance, the CityTracks research project used the cross-validation technique, with the k-fold method (with k = 10). This technique is well known and accepted and was used in related studies. The precision accuracy and recall accuracy were used as standard metrics to evaluate the classifiers algorithms' performance.

## Conclusion ##

The CityTracks mobile application is an innovative approach to collecting mobility data and identifying urban mobility patterns and modes. The data collection process and techniques used in the CityTracks research project demonstrate the importance of controlling the quality of the data collected and the value of pre-processing the data before analyzing it. The use of standard metrics to evaluate the classifiers algorithms' performance ensures reliable and valid results. The CityTracks research project provides valuable insights into urban mobility patterns and modes, enabling policymakers and city planners to make informed decisions to improve urban mobility.
