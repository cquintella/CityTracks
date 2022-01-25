//
//  CTLocationDB.m
//  CityTracks
//
//  Created by Carlos Quintella on 1/17/12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import "CTLocationDB.h"
#import "CTManager.h"
#import <netinet/in.h>
#define DEFAULT_BASE_FILENAME @"locationDB";




@implementation CTLocationDB
{
    NSMutableArray *locationDBCache;
    int numberOfFiles;
    NSString *locationDBFile;
}



@synthesize cacheSize = _cacheSize;
@synthesize currentRecord = _currentRecord;
@synthesize dBFileCounter = _dBFileCounter;



- (BOOL)isNetworkAvailableFlags:(SCNetworkReachabilityFlags *)outFlags {
    SCNetworkReachabilityRef    defaultRouteReachability;
    struct sockaddr_in          zeroAddress;
    
    bzero(&zeroAddress, sizeof(zeroAddress));
    zeroAddress.sin_len = sizeof(zeroAddress);
    zeroAddress.sin_family = AF_INET;
    
    defaultRouteReachability = SCNetworkReachabilityCreateWithAddress(NULL, (struct sockaddr *)&zeroAddress);
    
    SCNetworkReachabilityFlags flags;
    BOOL gotFlags = SCNetworkReachabilityGetFlags(defaultRouteReachability, &flags);
    if (!gotFlags) {
        return NO;
    }
    // kSCNetworkReachabilityFlagsReachable indicates that the specified nodename or address can
    // be reached using the current network configuration.
    BOOL isReachable = flags & kSCNetworkReachabilityFlagsReachable;
    
    // This flag indicates that the specified nodename or address can
    // be reached using the current network configuration, but a
    // connection must first be established.
    //
    // If the flag is false, we don't have a connection. But because CFNetwork
    // automatically attempts to bring up a WWAN connection, if the WWAN reachability
    // flag is present, a connection is not required.
    BOOL noConnectionRequired = !(flags & kSCNetworkReachabilityFlagsConnectionRequired);
    if ((flags & kSCNetworkReachabilityFlagsIsWWAN)) {
        noConnectionRequired = YES;
    }
    
    // Callers of this method might want to use the reachability flags, so if an 'out' parameter
    // was passed in, assign the reachability flags to it.
    if (outFlags) {
        *outFlags = flags;
    }
    
    return isReachable && noConnectionRequired;
}







- (BOOL) insertLocation:(NSDictionary *)newlocation
{
    
    if (self.currentRecord<self.cacheSize)
    {   
        [locationDBCache insertObject:newlocation atIndex:self.currentRecord];
        self.currentRecord++;


    }else
    { // Se o Array estiver cheio, faz o dump para arquivo e reseta o array.
        
        [self dumpLocationDB];

            
        locationDBFile=nil;
        locationDBCache = [[NSMutableArray alloc] initWithCapacity:self.cacheSize];
        self.currentRecord=0;
        [locationDBCache insertObject:newlocation atIndex:self.currentRecord];
        self.currentRecord++;
        
        [self uploadDb];
    }
    return TRUE;
}







- (BOOL) loadLocationDB
{
    
    //Carrega as ultimas localizações do arquivo 
    return 0;
}



- (BOOL) dumpLocationDB
{
    /* Grava os registros em um arquivo local para enviar futuramente */
    /* parte do nome do diretório */
    NSString *DBpath = [[CTManager sharedInstance] locationDBPath];
    /* adiciona o basefilename */
    NSString *baseFileName=DEFAULT_BASE_FILENAME;
  
    if (self.dBFileCounter>=10)
    {   
        self.dBFileCounter=0;
    }  
    NSString *fileWithExtension=[baseFileName stringByAppendingFormat:@".%d",self.dBFileCounter];
   locationDBFile=[DBpath stringByAppendingPathComponent:fileWithExtension];

    if (![locationDBCache writeToFile:locationDBFile atomically:NO])
    {    
        return FALSE;
    }
    NSLog(@"\n --- arquivo salvo: %@\n", locationDBFile);
    self.dBFileCounter ++;
    
    return TRUE;
    


    
}




-(BOOL) uploadDb
//precisa ser modificado para enviar todos os arquivos existentes no diretorio e apagar cada um que enviar.


{
    if ([self isNetworkAvailableFlags:NULL] == NO) {
        NSLog(@"Network not available, information submission postponed\n");
    }
    
    else {  
        NSLog(@"Connection available, preparing submission\n");
        /* Obtem a data e hora atual para passar como parâmetro no HTTP POST*/
        NSDate *now = [NSDate date];
        NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
        [dateFormatter setDateFormat:@"yyyy-MM-dd.HH:mm:ss"];
        NSString *currentDateAndTime = [dateFormatter stringFromDate:now];
        NSLog(@"Sending records to server at:%@", [currentDateAndTime description]);
    
        /*  Prepara o HTTP Post                                 */
        /*      a) URL*/
        NSString *urlAsString = @"http://";
        urlAsString=[urlAsString stringByAppendingString:[[CTManager sharedInstance] serverName]];
        urlAsString=[urlAsString stringByAppendingString:@"/postLocation.jsp"];
        
        /*      b) Prepara parâmetros do HTTP Request           */
        urlAsString = [urlAsString stringByAppendingString:@"?deviceId="];
        urlAsString = [urlAsString stringByAppendingString: [[CTManager sharedInstance] deviceId]];
        urlAsString = [urlAsString stringByAppendingString:@"&TimeStamp="];
        urlAsString = [urlAsString stringByAppendingString:[currentDateAndTime description]];
    NSLog(@"connecting to: %@",urlAsString);
    NSURL *url = [NSURL URLWithString:urlAsString];
    NSMutableURLRequest *urlRequest = [NSMutableURLRequest requestWithURL:url];
    /* timeout e método do HTTP */
    [urlRequest setTimeoutInterval:30.0f];
    [urlRequest setHTTPMethod:@"POST"];
    /* o BODY Para enviar no POST */
    NSError *theError = nil;
    
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:[NSArray arrayWithContentsOfFile:locationDBFile] options:NSJSONWritingPrettyPrinted error:&theError];
    
    if ([jsonData length]>0 && theError==nil) {
        
       
        NSString *jsonString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
        NSString *body=jsonString;
         NSLog(@"Successfully serialized the dictionary into data. %@",jsonString);
        [urlRequest setHTTPBody:[body dataUsingEncoding:NSUTF8StringEncoding]];
        NSOperationQueue *queue = [[NSOperationQueue alloc] init];
        [NSURLConnection
         sendAsynchronousRequest:urlRequest queue:queue completionHandler:^(NSURLResponse *response,
                                                                            NSData *data, NSError *error) {
             if ([data length] >0 && error == nil){
                 NSString *html = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
                 NSLog(@"HTML = %@", html); }
             else if ([data length] == 0 && error == nil){
                 NSLog(@"Nothing was downloaded."); }
             else if (error != nil){
                 NSLog(@"Error happened = %@", error);
             } }];
        return TRUE;
    }else
        NSLog(@"erro criando JSON DATA.\n");
    return FALSE;
    }
    return FALSE;
}
    
    


    
    
    





- (BOOL) resetLocationDB
{ 
    //Esvazia a lista e o arquivo de localizações
    return 0; 
}






- (id)initWithPath: (NSString *) pathToDb andCacheSize: (int) theCacheSize
{
    NSLog(@">>Inicializando o Array de Armazenagem\n");
    self.cacheSize=theCacheSize;
    locationDBCache = [[NSMutableArray alloc] initWithCapacity:self.cacheSize];

    self.currentRecord=0;
    self.dBFileCounter=0;

    self=[super init];
    return self;
}

@end
