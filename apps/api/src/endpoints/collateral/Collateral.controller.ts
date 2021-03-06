import {
    AmbrosiaResponseOK,
    CollateralCreateRequestRuntime,
    CollateralResponse,
    okResponse,
} from '@api/io-model';
import {
    Body,
    Controller,
    Get,
    Post,
    Put,
    Query,
    StreamableFile,
    UploadedFiles,
    UseInterceptors,
} from '@nestjs/common';
import { AnyFilesInterceptor } from '@nestjs/platform-express';
import { existsSync, mkdirSync } from 'fs';
import { join } from 'path';
import sharp from 'sharp';

import { collateralQuery } from '../../database/entity/collateral/query/Collateral.query';
import { ControllerBase } from '../base/ControllerBase';
import { EndpointUrls } from '../EndpointUrls';

const folder = join('data', 'collateral');
mkdirSync(folder, { recursive: true });

@Controller(EndpointUrls.api.collateral.url)
@UseInterceptors(AnyFilesInterceptor())
export class CollateralController extends ControllerBase {
    @Post('/create')
    async create(
        @Body() request: CollateralCreateRequestRuntime
    ): Promise<CollateralResponse> {
        const entity = await collateralQuery.create(request);
        const collateral = collateralQuery.toSimple(entity);
        return { collateral, ...okResponse };
    }
    @Get('/image')
    async getImage(@Query('uuid') uuid: string): Promise<StreamableFile> {
        if (!uuid) this.exception.badRequest({ uuid });
        const filePath = this.getPath(uuid);
        if (!existsSync(filePath)) return null;
        return new StreamableFile(await sharp(filePath).png().toBuffer(), {
            disposition: 'image.png',
        });
    }
    @Post('/image')
    async postImage(
        @Query('uuid') uuid: string,
        @UploadedFiles() files: Array<Express.Multer.File>
    ): Promise<AmbrosiaResponseOK> {
        if (!uuid) this.exception.badRequest('uuid=' + uuid);
        const filePath = this.getPath(uuid);
        sharp(files[0].buffer).toFile(filePath);
        return okResponse;
    }
    getPath(uuid: string): string {
        return join(folder, uuid + '.png');
    }
}
