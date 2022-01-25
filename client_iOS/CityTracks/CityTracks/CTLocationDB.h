//
//  CTLocationDB.h
//  CityTracks
//
//  Created by Carlos Quintella on 1/17/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreLocation/CoreLocation.h>
#import <SystemConfiguration/SystemConfiguration.h>
//https://github.com/rifatdover/ReachabilityForIOS5




#define DEFAULT_CACHE_SIZE_VALUE 1000
@interface CTLocationDB : NSObject

{


}


@property  int cacheSize;
@property  int currentRecord;
@property int dBFileCounter;





- (BOOL) loadLocationDB;

- (BOOL) dumpLocationDB;

- (BOOL)insertLocation:(NSDictionary *) newlocation;

- (BOOL) uploadDb;

- (BOOL) resetLocationDB;

- (id)initWithPath: (NSString *) pathToDB andCacheSize: (int) cacheSize;



@end
