<?php

namespace App\Http\Controllers;

use Illuminate\Http\Request;
use Illuminate\Http\Response;
use Illuminate\Support\Facades\DB;
use Illuminate\Support\Str;

class AuthenticationController extends Controller
{
    /**
     * Main authentication endpoint.
     *
     * Authenticates user using their username & password, or username & token.
     *
     * @param Request $request HTTP-request Object.
     * @return JSON response with an authentication result.
     *
     * Authentication succeed:
     *
     *  - username
     *  - token
     *  - uuid
     *  - accessToken
     *
     * Authentication failed:
     *
     *  - errorMessage
     *
     */
    public function authenticate(Request $request)
    {
        if ($request->filled(['username', 'password'])) {

            // Authentication request using username & password

            $username = $request->input('username');
            $password = $request->input('password');

            // Find a user with the given name
            $user = DB::table('xf_user')->where('username', $username)->first();

            if (!empty($user)) {

                $credentials  = DB::table('xf_user_authenticate')->where('user_id', $user->user_id)->first();
                $unserialized = unserialize($credentials->data);

                if (password_verify($password, $unserialized['hash'])) {

                    // Find a player with the received user ID
                    $player = DB::table('players')->where('user_id', $user->user_id)->first();

                    // Update token & access token
                    $token        = str_replace('-', '', Str::uuid());
                    $access_token = str_replace('-', '', Str::uuid());

                    if (empty($player)) {

                        $uuid  = str_replace('-', '', Str::uuid());

                        DB::table('players')
                            ->updateOrInsert(
                                ['user_id' => $user->user_id],
                                ['token' => $token, 'uuid' => $uuid, 'access_token' => $access_token]
                            );
                    } else {

                        $uuid = $player->uuid;

                        DB::table('players')
                            ->updateOrInsert(
                                ['user_id' => $user->user_id],
                                ['token' => $token, 'uuid' => $uuid, 'access_token' => $access_token]
                            );
                    }

                    return response([
                        'username'    => $username,
                        'token'       => $token,
                        'uuid'        => $uuid,
                        'accessToken' => $access_token
                    ]);
                }

                return response(['errorMessage' => 'Неверный логин или пароль.'], 400);
            }

            return response(['errorMessage' => 'Пользователь с такими данными не найден.'], 400);

        } elseif ($request->filled(['username', 'token'])) {

            // Authentication request using username & token

            $username = $request->input('username');
            $token    = $request->input('token');

            // Find a user with the given name
            $user = DB::table('xf_user')->where('username', $username)->first();

            if (!empty($user)) {

                // Find a player with the received user ID & given token
                $player = DB::table('players')->where('user_id', $user->user_id)->where('token', $token)->first();

                // Update access token
                $access_token = str_replace('-', '', Str::uuid());

                if (!empty($player)) {

                    $uuid = $player->uuid;

                    DB::table('players')
                        ->updateOrInsert(
                            ['user_id' => $user->user_id],
                            ['token' => $token, 'uuid' => $uuid, 'access_token' => $access_token]
                        );

                    return response([
                        'username'    => $username,
                        'token'       => $token,
                        'uuid'        => $uuid,
                        'accessToken' => $access_token
                    ]);
                }

                return response(['errorMessage' => 'Игрок с такими данными не найден. Попробуйте заново ввести логин и пароль.'], 400);
            }

            return response(['errorMessage' => 'Пользователь с такими данными не найден. Попробуйте заново ввести логин и пароль.'], 400);
        }
    }

    /**
     * Search for player and change their server ID. (Client request handler)
     *
     * @param Request $request (accessToken, selectedProfile, serverId)
     * @return Response If everything goes well, the client will receive a "HTTP/1.1 204 No Content" response.
     */
    public function join(Request $request)
    {
        if ($request->filled(['accessToken', 'selectedProfile', 'serverId'])) {

            $access_token = $request->input('accessToken');
            $uuid         = $request->input('selectedProfile');
            $server_id    = $request->input('serverId');

            $player = DB::table('players')->where('access_token', $access_token)->where('uuid', $uuid)->first();

            if (!empty($player)) {

                $user = DB::table('xf_user')->where('user_id', $player->user_id)->first();

                DB::table('players')
                    ->updateOrInsert(
                        ['user_id' => $user->user_id, 'uuid' => $uuid, 'access_token' => $access_token],
                        ['server_id' => $server_id]
                    );

                return response(null, 204);

            } else {
                return response([
                    'error'        => 'Ошибка аутентификации.',
                    'errorMessage' => 'Запись с таким accessToken и UUID отсутствует.',
                    'cause'        => 'Неверный accessToken или UUID игрока.'
                ], 400);
            }
        } else {
            return response([
                'error'        => 'Ошибка запроса к серверу.',
                'errorMessage' => 'Некорректный запрос к серверу аутентификации.',
                'cause'        => 'Не поддерживаемый массив значений.'
            ], 400);
        }
    }

    /**
     * Server checks if the user has authenticated.
     *
     * @param Request $request (username, serverId)
     * @return Response The response is a JSON object containing the user's UUID and skin blob.
     */
    public function hasJoined(Request $request)
    {
        if ($request->filled(['username', 'serverId'])) {

            $username  = $request->input('username');
            $server_id = $request->input('serverId');

            $user = DB::table('xf_user')->where('username', $username)->first();

            if (!empty($user)) {

                $player = DB::table('players')->where('user_id', $user->user_id)->where('server_id', $server_id)->first();

                if (!empty($player)) {

                    $base64 = '{"timestamp":' . time() . '","profileId":"' . $player->uuid . '","profileName":"' . $user->username . '","textures":{"SKIN":{"url":"https://cloud.minecraft.biz/skins/default.png"}}}';

                    return response([
                        'id' => $player->uuid,
                        'name' => $username,
                        'properties' => [
                            'name' => 'textures',
                            'value' => base64_encode($base64)
                        ]
                    ]);

                } else {
                    return response([
                        'error'        => 'Ошибка аутентификации.',
                        'errorMessage' => 'Запись с таким идентификатором сервера отсутствует.',
                        'cause'        => 'Неверный идентификатор сервера.'
                    ], 400);
                }
            } else {
                return response([
                    'error'        => 'Ошибка аутентификации.',
                    'errorMessage' => 'Запись с таким именем пользователя отсутствует.',
                    'cause'        => 'Неверное имя пользователя.'
                ], 400);
            }
        } else {
            return response([
                'error'        => 'Ошибка запроса к серверу.',
                'errorMessage' => 'Некорректный запрос к серверу аутентификации.',
                'cause'        => 'Не поддерживаемый массив значений.'
            ], 400);
        }
    }
}