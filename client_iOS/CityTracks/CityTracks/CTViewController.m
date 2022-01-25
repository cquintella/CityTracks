//
//  CTViewController.m
//  CityTracks
//
//  Created by Carlos Quintella on 11/29/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//



#import "CTViewController.h"


#define DEFAULT_DISTANCE_FILTER_VALUE 10.0
#define DEFAULT_HEADING_FILTER_VALUE 5.0


@implementation CTViewController


// OUTLETS

    @synthesize myMapView;
    @synthesize lbLatitude;
    @synthesize lbLongitude;
    @synthesize lbTracking;
@synthesize lbTimeStamp;


// PROPERTIES

    @synthesize currentLocation;
    @synthesize locationManager;
    @synthesize accuracyLevel;




// METHODS

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    
}






#pragma mark - View lifecycle


- (void)viewDidLoad
{

    
    
    [super viewDidLoad];
 
    //[CTManager sharedInstance];
    
    
    
	// Do any additional setup after loading the view, typically from a nib.
    NSLog(@"CTViewController viewDidLoad\n");
    
    
    // Initializing and Stating the Location Manager...
    NSLog(@"Initializing LocationManager\n");
    
    if([CLLocationManager locationServicesEnabled])
    {
        
        if (locationManager == nil) 
            self.locationManager=[[CLLocationManager alloc] init ];
        locationManager.delegate = self;
        locationManager.desiredAccuracy= kCLLocationAccuracyBestForNavigation;
        [CLLocationManager locationServicesEnabled];
        locationManager.purpose = @"for researching people mobility in large cities.";
        // Define o deslocamento minimo necessário para que o LocationManager seja notificado
        locationManager.distanceFilter=DEFAULT_DISTANCE_FILTER_VALUE;
       // locationManager.headingFilter=DEFAULT_HEADING_FILTER_VALUE;

        [locationManager startUpdatingLocation];
       // [locationManager startUpdatingHeading]; /* não preciso de heading agora */
    } else
    {
        NSLog(@"Location Manager is not available");
        /* Informar ao usuário e terminar a aplicação */
    }

    
    // Initializing MapView
    NSLog(@"   2.MapView\n");
    [myMapView setMapType:MKMapTypeSatellite];
    self.myMapView.delegate=self;
    
    
    //SETANDO A REGIAO MOSTRADA NO MAPA
    //     MKCoordinateRegion region = { {0.0, 0.0 }, { 0.0, 0.0 } };
    //     [myMapView regionThatFits:region];
    //     [myMapView setRegion:region animated:YES];
    // [myMapView setCenterCoordinate:{22,43} animated:TRUE];
    
}





- (void)locationManager:        (CLLocationManager *)manager
        didUpdateToLocation:    (CLLocation *)newLocation
        fromLocation:           (CLLocation *)oldLocation
{
    
// test that the horizontal accuracy does not indicate an invalid measurement
    if (newLocation.horizontalAccuracy < 0) 
    {
        NSLog (@"Cant get my current location, gps not working?"); 
        return;
    }
    
//  test the age of the location measurement to determine if the measurement is cached
//      - in most cases you will not want to rely on cached measurements

    NSTimeInterval locationAge = -[newLocation.timestamp timeIntervalSinceNow];
    if (locationAge <= 5.0) 
    {
        /* Verificar se a horizontal accurancy é boa */


        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd.HH:mm:ss"];


        
        NSDictionary *locationDict=[NSDictionary dictionaryWithObjectsAndKeys:
                                    [NSString stringWithFormat:@"%f",newLocation.coordinate.latitude],@"latitude",
                                    [NSString stringWithFormat:@"%f",newLocation.coordinate.longitude], @"longitude",
                                    [NSString stringWithFormat:@"%f",newLocation.altitude,newLocation.altitude],@"altitude",
                                    [NSString stringWithFormat:@"%@",[dateFormatter stringFromDate:newLocation.timestamp]], @"timestamp",
                                    [NSString stringWithFormat:@"%f",newLocation.horizontalAccuracy], @"horizontal-accuracy",
                                    [NSString stringWithFormat:@"%f",newLocation.verticalAccuracy],@"vertical-accuracy",
                                    [NSString stringWithFormat:@"%f",newLocation.speed],@"speed",
                                    [NSString stringWithFormat:@"%f",newLocation.course],   @"course", nil];
        

        
        [[[CTManager sharedInstance] myLocationDB] insertLocation:locationDict];
        
        self.currentLocation = [newLocation coordinate];
        
        MKCoordinateRegion region = { newLocation.coordinate, {0.009,0.009}};
        [myMapView regionThatFits:region];
        [myMapView setRegion:region animated:NO];
        lbLatitude.text = [NSString stringWithFormat:@"%f",newLocation.coordinate.latitude];
        lbLongitude.text = [NSString stringWithFormat:@"%f",newLocation.coordinate.longitude];
        lbTimeStamp.text = [NSString stringWithFormat:@"%f",newLocation.timestamp];
       
    }else
    {
        NSLog (@"Ancient Mesurement: %@", newLocation.timestamp);
        /* muito antigo, melhor esperar um mais recente */
    }

    
    return;
}


- (void)locationManager:(CLLocationManager *)manager
       didFailWithError:(NSError *)error
{
	NSLog(@"Location Manager failed - Error: %@", [error description]);
}









- (int) changeTrackingAccurancy: (int) newAccuracyLevel
{ 
    NSLog(@"changeTrackingAccurancy to:%d\n", newAccuracyLevel);
    /*
    switch (newAccuracyLevel) {
        case 1:
            [locationManager stopUpdatingLocation];
            lbTracking.text=@"Tracking Off";
            break;
        case 2:
            locationManager.desiredAccuracy=  kCLLocationAccuracyHundredMeters;
            lbTracking.text=@"Tracking Low";
            break;
        case 3:
            locationManager.desiredAccuracy=  kCLLocationAccuracyNearestTenMeters;
            lbTracking.text=@"Tracking Medium";
            break;
        case 4:
            locationManager.desiredAccuracy=  kCLLocationAccuracyBest;
            lbTracking.text=@"Tracking High";
            break;
        default:
            locationManager.desiredAccuracy=kCLLocationAccuracyBestForNavigation;
            lbTracking.text=@"Tracking on MAX";
            break;
    }
    
    if (newAccuracyLevel <=0)
        locationManager.desiredAccuracy= kCLLocationAccuracyBestForNavigation;
    else if (newAccuracyLevel <=1)
        locationManager.desiredAccuracy= kCLLocationAccuracyBest;
    else if (newAccuracyLevel <= 2)
        locationManager.desiredAccuracy=kCLLocationAccuracyNearestTenMeters;
    else if(newAccuracyLevel<=3)
        locationManager.desiredAccuracy=kCLLocationAccuracyHundredMeters;
    else if (newAccuracyLevel<=4)
        locationManager.desiredAccuracy=kCLLocationAccuracyKilometer;
    else if (newAccuracyLevel<=5)
        locationManager.desiredAccuracy=kCLLocationAccuracyThreeKilometers;
    else
    {   
        locationManager.desiredAccuracy= kCLLocationAccuracyBestForNavigation;
        return 1;
    }*/
    return 0;
}










- (void)viewDidUnload
{

 //   [self setMyMapView:nil];
    [self setMyMapView:nil];
    [self setLbLatitude:nil];
    [self setLbLongitude:nil];
    [self setLbTracking:nil];
    [self setLbTimeStamp:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}




- (void)viewWillAppear:(BOOL)animated
{
    int newAccuracyLevel=[[CTManager sharedInstance] accuracyLevel];
    [super viewWillAppear:animated];
    if (newAccuracyLevel <=1){
        locationManager.desiredAccuracy= kCLLocationAccuracyNearestTenMeters;
         [locationManager stopUpdatingHeading];
         [locationManager stopUpdatingLocation];
         lbTracking.text=@"Off";
         }
    else if (newAccuracyLevel <= 2){
        locationManager.desiredAccuracy=kCLLocationAccuracyKilometer;
        [locationManager startUpdatingHeading];
        [locationManager startUpdatingLocation];
        lbTracking.text=@"low";
    }
        else if(newAccuracyLevel<=3){
        locationManager.desiredAccuracy=kCLLocationAccuracyHundredMeters;
            [locationManager startUpdatingHeading];
            [locationManager startUpdatingLocation];
             lbTracking.text=@"medium";
        }
            else if (newAccuracyLevel<=4){
                locationManager.desiredAccuracy=kCLLocationAccuracyNearestTenMeters;
                [locationManager startUpdatingHeading];
                [locationManager startUpdatingLocation];
                 lbTracking.text=@"high";
            }
    else
    {   
        locationManager.desiredAccuracy= kCLLocationAccuracyBest;
        [locationManager startUpdatingHeading];
        [locationManager startUpdatingLocation];
         lbTracking.text=@"maximum";
    }    
    return;
    
    
}









- (void)viewDidAppear:(BOOL)animated
{
    [super viewDidAppear:animated];
}

- (void)viewWillDisappear:(BOOL)animated
{

	[super viewWillDisappear:animated];
}

- (void)viewDidDisappear:(BOOL)animated
{
	[super viewDidDisappear:animated];
}




- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    // Return YES for supported orientations
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
    } else {
        return YES;
    }
}

@end
