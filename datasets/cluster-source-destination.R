#mt.test<-data.frame(NA,ncol=1,nrow=nrow(segments))
#mt.test$lt<-segments$start_lat
#mt.test$lg<-segments$start_long
library(tidyverse)
library("leaflet")
k =1
# VAMOS PEGAR AS ORIGENS
print("Origens")

mx.test<-matrix(ncol=2,nrow=nrow(segments))
mx.test[,1]<-segments$start_lat
mx.test[,2]<-segments$start_long


result.source<-kmeans(mx.test,k)
#result.source<-kmedoids(mx.test, k)

df.points.source <- data.frame(  lat = result.source$centers[,1],
                                 long = result.source$centers[,2])

#VAMOS PEGAR OS DESTINOS
print("Destinos")
k=20
mx.test<-matrix(ncol=2,nrow=nrow(segments))
mx.test[,1]<-segments$end_lat
mx.test[,2]<-segments$end_long

result.destination<-kmeans(mx.test,k)
#result.destination<-kmedoids(mx.test, k)

df.points.destination <- data.frame(  lat = result.source$centers[,1],
                                 long = result.source$centers[,2])

print("Mapas")
mapa <- leaflet() %>% 
  addProviderTiles(providers$HERE.basicMap) %>% addTiles()
mapa %>%
  # local
  fitBounds( lng1 = min.long, 
             lat1 = min.lat, 
             lng2 = max.long, 
             lat2 = max.lat) %>%
  addMeasure(   position = "bottomleft",
                primaryLengthUnit = "meters",
                primaryAreaUnit = "sqmeters",
                activeColor = "#3D535D",
                completedColor = "#7D4479")
mapa  %>% addAwesomeMarkers(data=df.points.source, lng = ~long, lat = ~lat)
mapa  %>% addMarkers(data=df.points.destination, lng = ~long, lat = ~lat)
