//
//  CTManager.h
//  CityTracks
//
//  Created by Carlos Quintella on 11/29/11.
//  Copyright (c) 2011 __MyCompanyName__. All rights reserved.
//



// CTManager é um singleton que controla as variáveis globais.

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import "UIDevice+IdentifierAddition.h"
#import "CTLocationDB.h"

#define DEFAULT_REPORTING_FREQUENCY 5
#define DEFAULT_ACCURACY_LEVEL 5
#define DEFAULT_SERVER_PORT 8080
#define DEFAULT_LOCATION_CACHE_SIZE 6.0
#define DEFAULT_SERVER_NAME 172.30.182.152


//Modelo de Singleton:
        //http://iphone.galloway.me.uk/iphone-sdktutorials/singleton-classes/





@interface CTManager : NSObject
{

}


@property (nonatomic)int reportingFrequency;
@property (nonatomic)int locationDBCacheSize;
@property (nonatomic) int accuracyLevel;

@property (nonatomic, strong) NSString *deviceId;
@property (nonatomic, strong) NSString *locationDBPath;
@property (nonatomic, strong) NSString *serverName;
@property (nonatomic, strong) CTLocationDB *myLocationDB;


/* Método de Classe que permite o acesso a sharedInstance */
+ (id) sharedInstance;






@end
