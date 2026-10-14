export interface ApiResponse<T>{
    success: boolean;
    message: string;
    data?: T;
    errorStatusCode?:number;
    errorCode?:string;
    timestamp:string;
    validationErrors?:Record<string,string>;
}