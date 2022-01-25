//
//  CTViewController.h
//  CityTracks
//
//  Created by Carlos Quintella on 11/29/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <MapKit/MKMapView.h>
#import <CoreLocation/CoreLocation.h>
#import "CTLocationDB.h"
#import "CTManager.h"
//#import "CTCommunicator.h"



@interface CTViewController : UIViewController <CLLocationManagerDelegate, MKMapViewDelegate> 
{
    //Variáveis de Instância Visíveis.
    CTManager           *mySharedManager;
    CTLocationDB        *myLocationDB;

}




// OUTLETS
@property (strong, nonatomic)   IBOutlet    MKMapView *myMapView;
@property (strong, nonatomic)   IBOutlet    UILabel *lbLatitude;
@property (strong, nonatomic)   IBOutlet    UILabel *lbLongitude;
@property (strong, nonatomic)   IBOutlet    UILabel *lbTracking;
@property (strong, nonatomic) IBOutlet UILabel *lbTimeStamp;


// PROPERTIES
@property (nonatomic)           CLLocationCoordinate2D      currentLocation;         
@property (strong,nonatomic)    CLLocationManager           *locationManager;
@property (nonatomic)           int                         accuracyLevel;

//___________________________________________________________
//                   METHODS
//___________________________________________________________


- (void)locationManager:(CLLocationManager *)manager didUpdateToLocation:(CLLocation *)newLocation fromLocation:(CLLocation *)oldLocation;

- (void)locationManager:(CLLocationManager *)manager didFailWithError:(NSError *)error;

- (int) changeTrackingAccurancy:(int) newAccuracyLevel;

//- (void)locationManager:(CLLocationManager *)manager didUpdateHeading:(CLHeading                                                                       *)newHeading ;

@end
